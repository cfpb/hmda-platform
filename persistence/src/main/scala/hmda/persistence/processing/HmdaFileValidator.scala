package hmda.persistence.processing

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.{ ask, pipe }
import akka.persistence.SnapshotOffer
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.model.validation._
import hmda.persistence.HmdaSupervisor.{ FindHmdaFiling, FindProcessingActor }
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.PaginatedResource
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.institutions.InstitutionCommands.GetInstitutionById
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.ProcessingMessages.{ BeginValidation, CompleteValidation, ValidationCompleted, ValidationCompletedWithErrors }
import hmda.util.SourceUtils
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine
import hmda.validation.engine.ts.TsEngine
import hmda.validation.rules.lar.`macro`.MacroEditTypes._
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents._
import hmda.persistence.messages.events.processing.HmdaFileParserEvents.{ LarParsed, TsParsed }
import hmda.persistence.messages.events.processing.HmdaFileValidatorEvents._
import hmda.persistence.messages.events.validation.SubmissionLarStatsEvents.MacroStatsUpdated
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SubmissionManager.GetActorRef
import hmda.validation.stats.SubmissionLarStats.PersistStatsForMacroEdits
import hmda.validation.stats.ValidationStats.AddSubmissionTaxId
import hmda.validation.stats.SubmissionLarStats
import HmdaFileWorker._
import hmda.persistence.messages.commands.processing.HmdaFileValidatorState._

import scala.util.Try
import scala.concurrent.duration._

object HmdaFileValidator {

  val name = "HmdaFileValidator"

  case class ValidationStarted(submissionId: SubmissionId) extends Event

  case class ValidateMacro(source: LoanApplicationRegisterSource, replyTo: ActorRef) extends Command

  case class ValidateAggregate(ts: TransmittalSheet) extends Command

  case class CompleteMacroValidation(errors: LarValidationErrors, replyTo: ActorRef) extends Command

  case class VerifyEdits(editType: ValidationErrorType, verified: Boolean, replyTo: ActorRef) extends Command

  case class GetNamedErrorResultsPaginated(editName: String, page: Int) extends Command

  case object GetSVState extends Command

  case object GetQMState extends Command

  case object GetValidatedLines extends Command

  def props(supervisor: ActorRef, validationStats: ActorRef, id: SubmissionId): Props = Props(new HmdaFileValidator(supervisor, validationStats, id))

  def createHmdaFileValidator(system: ActorSystem, supervisor: ActorRef, validationStats: ActorRef, id: SubmissionId): ActorRef = {
    system.actorOf(HmdaFileValidator.props(supervisor, validationStats, id).withDispatcher("persistence-dispatcher"))
  }

  case class PaginatedErrors(errors: Seq[ValidationError], totalErrors: Int)

}

class HmdaFileValidator(supervisor: ActorRef, validationStats: ActorRef, submissionId: SubmissionId)
    extends HmdaPersistentActor with TsEngine with LarEngine with SourceUtils {

  import HmdaFileValidator._

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor-lookup-timeout")
  val processingParallelism = config.getInt("hmda.processing.parallelism")

  implicit val timeout = Timeout(duration.seconds)
  val parserPersistenceId = s"${HmdaFileParser.name}-$submissionId"

  var counter = 0
  val snapshotCounter = config.getInt("hmda.journal.snapshot.counter")

  var institution: Option[Institution] = Some(Institution.empty.copy(id = submissionId.institutionId))

  def ctx: ValidationContext = ValidationContext(institution, Try(Some(submissionId.period.toInt)).getOrElse(None))

  override def preStart(): Unit = {
    super.preStart()
    val fInstitutions = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
    for {
      a <- fInstitutions
      i <- (a ? GetInstitutionById(submissionId.institutionId)).mapTo[Option[Institution]]
    } yield institution = i
  }

  var verificationState = HmdaVerificationState()
  var svState = SVState()
  var qmState = QMState()

  val fHmdaFiling = (supervisor ? FindHmdaFiling(submissionId.period)).mapTo[ActorRef]

  def statRef = for {
    manager <- (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]
    stat <- (manager ? GetActorRef(SubmissionLarStats.name)).mapTo[ActorRef]
  } yield stat

  override def updateState(event: Event): Unit = {
    event match {
      case event: TsSyntacticalError => svState = svState.updated(event)
      case event: LarSyntacticalError => svState = svState.updated(event)
      case event: TsValidityError => svState = svState.updated(event)
      case event: LarValidityError => svState = svState.updated(event)

      case event: TsQualityError => qmState = qmState.updated(event)
      case event: LarQualityError => qmState = qmState.updated(event)
      case event: LarMacroError => qmState = qmState.updated(event)

      case event: EditsVerified => verificationState = verificationState.updated(event)
      case event: TsValidated => verificationState = verificationState.updated(event)
      case event: LarValidated => verificationState = verificationState.updated(event)
    }
  }

  override def persistenceId: String = s"$name-$submissionId"

  override def receiveCommand: Receive = {

    case BeginValidation(replyTo) =>
      sender() ! ValidationStarted(submissionId)
      events(parserPersistenceId)
        .filter(x => x.isInstanceOf[TsParsed])
        .map { e => e.asInstanceOf[TsParsed].ts }
        .map { ts =>
          self ! ts
          validationStats ! AddSubmissionTaxId(ts.taxId, submissionId)
          self ! ValidateAggregate(ts)
          validateTs(ts, ctx).toEither
        }
        .map {
          case Right(_) => // do nothing
          case Left(errors) => TsValidationErrors(errors.list.toList)
        }
        .runWith(Sink.actorRef(self, NotUsed))

      val larSource: Source[LoanApplicationRegister, NotUsed] = events(parserPersistenceId)
        .filter(x => x.isInstanceOf[LarParsed])
        .map(e => e.asInstanceOf[LarParsed].lar)

      larSource
        .via(balancer(validate(ctx, self), processingParallelism))
        .map {
          case Right(_) => //do nothing
          case Left(errors) => LarValidationErrors(errors.list.toList)
        }
        .runWith(Sink.actorRef(self, ValidateMacro(larSource, replyTo)))

    case ValidateAggregate(ts) =>
      performAsyncChecks(ts, ctx)
        .map(validations => validations.toEither)
        .map {
          case Right(_) => // do nothing
          case Left(errors) => self ! TsValidationErrors(errors.list.toList)
        }

    case ts: TransmittalSheet =>
      persist(TsValidated(ts)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case lar: LoanApplicationRegister =>
      val validated = LarValidated(lar, submissionId)
      persist(validated) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
        for {
          f <- fHmdaFiling
          stat <- statRef
        } yield {
          f ! validated
          stat ! validated
        }
      }

    case ValidateMacro(larSource, replyTo) =>
      log.debug("Quality Validation completed")
      for {
        stat <- statRef
        _ <- (stat ? PersistStatsForMacroEdits).mapTo[MacroStatsUpdated]
        fMacro = checkMacro(larSource, ctx)
          .mapTo[LarSourceValidation]
          .map(larSourceValidation => larSourceValidation.toEither)
          .map {
            case Right(_) => CompleteValidation(replyTo)
            case Left(errors) => CompleteMacroValidation(LarValidationErrors(errors.list.toList), replyTo)
          }

      } yield {
        fMacro pipeTo self
      }

    case tsErrors: TsValidationErrors =>
      val errors = tsErrors.errors
      val syntacticalErrors = errorsOfType(errors, Syntactical)
        .map(e => TsSyntacticalError(e))
      persistErrors(syntacticalErrors)

      val validityErrors = errorsOfType(errors, Validity)
        .map(e => TsValidityError(e))
      persistErrors(validityErrors)

      val qualityErrors = errorsOfType(errors, Quality)
        .map(e => TsQualityError(e))
      persistErrors(qualityErrors)

    case larErrors: LarValidationErrors =>
      if (counter > snapshotCounter) {
        log.debug(s"Saving snapshot for $submissionId")
        saveSnapshot(svState)
        saveSnapshot(qmState)
        saveSnapshot(verificationState)
        counter = 0
      }
      counter += 1
      val errors = larErrors.errors
      val syntacticalErrors = errorsOfType(errors, Syntactical)
        .map(e => LarSyntacticalError(e))
      persistErrors(syntacticalErrors)

      val validityErrors = errorsOfType(errors, Validity)
        .map(e => LarValidityError(e))
      persistErrors(validityErrors)

      val qualityErrors = errorsOfType(errors, Quality)
        .map(e => LarQualityError(e))
      persistErrors(qualityErrors)

      val macroErrors = errorsOfType(errors, Macro)
        .map(e => LarMacroError(e))
      persistErrors(macroErrors)

    case CompleteMacroValidation(e, replyTo) =>
      self ! LarValidationErrors(e.errors)
      self ! CompleteValidation(replyTo)

    case CompleteValidation(replyTo, originalSender) =>
      if (readyToSign) {
        log.debug(s"Validation completed for $submissionId")
        replyTo ! ValidationCompleted(originalSender)
      } else {
        log.debug(s"Validation completed for $submissionId, errors found")
        replyTo ! ValidationCompletedWithErrors(originalSender)
      }

    case VerifyEdits(editType, v, replyTo) =>
      val client = sender()
      if (editType == Quality || editType == Macro) {
        persist(EditsVerified(editType, v)) { e =>
          updateState(e)
          self ! CompleteValidation(replyTo, Some(client))
        }
      } else client ! None

    case GetState => sender() ! verificationState
    case GetSVState => sender() ! svState
    case GetQMState => sender() ! qmState

    case GetNamedErrorResultsPaginated(editName, page) =>
      val replyTo = sender()
      val allFailures = allEditsByName(editName)
      count(allFailures).map { total =>
        val p = PaginatedResource(total)(page)
        val pageOfFailuresF = allFailures.take(p.toIndex).drop(p.fromIndex).runWith(Sink.seq)
        pageOfFailuresF.map { pageOfFailures =>
          replyTo ! PaginatedErrors(pageOfFailures, total)
        }
      }

    case Shutdown =>
      context stop self

  }

  def readyToSign: Boolean = {
    val svReady = !svState.containsSVEdits
    val qualityReady: Boolean = verificationState.qualityVerified || qmState.qualityEdits.isEmpty
    val macroReady: Boolean = verificationState.macroVerified || qmState.macroEdits.isEmpty

    svReady && qualityReady && macroReady
  }

  override def receiveRecover: Receive = super.receiveRecover orElse {
    case SnapshotOffer(_, sv: SVState) =>
      log.debug("Recovering SVState")
      svState = sv
    case SnapshotOffer(_, qm: QMState) =>
      log.debug("Recovering QMState")
      qmState = qm
    case SnapshotOffer(_, v: HmdaVerificationState) =>
      log.debug("Recovering HmdaVerificationState")
      verificationState = v
  }

  private def allEditsByName(name: String): Source[ValidationError, NotUsed] = {
    val edits = events(persistenceId).map {
      case LarSyntacticalError(err) if err.ruleName == name => err
      case TsSyntacticalError(err) if err.ruleName == name => err
      case LarValidityError(err) if err.ruleName == name => err
      case TsValidityError(err) if err.ruleName == name => err
      case LarQualityError(err) if err.ruleName == name => err
      case TsQualityError(err) if err.ruleName == name => err
      case LarMacroError(err) if err.ruleName == name => err
      case _ => EmptyValidationError
    }
    edits.filter(_ != EmptyValidationError)
  }

  private def persistErrors(errors: Seq[Event]): Unit = {
    errors.foreach { error =>
      persist(error) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }
    }
  }

  private def errorsOfType(errors: Seq[ValidationError], errorType: ValidationErrorType): Seq[ValidationError] = {
    errors.filter(_.errorType == errorType)
  }
}
