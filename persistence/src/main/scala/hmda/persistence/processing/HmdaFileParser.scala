package hmda.persistence.processing

import akka.pattern.ask
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.lar.{ LarCsvParser, LarParsingError }
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.PaginatedResource
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.processing.HmdaRawFile.LineAdded
import hmda.persistence.processing.ProcessingMessages._

import scala.concurrent.duration._

object HmdaFileParser {

  val name = "HmdaFileParser"

  case class ReadHmdaRawFile(persistenceId: String, replyTo: ActorRef) extends Command
  case class FinishParsing(replyTo: ActorRef) extends Command
  case class TsParsed(ts: TransmittalSheet) extends Event
  case class TsParsedErrors(errors: List[String]) extends Event
  case class LarParsed(lar: LoanApplicationRegister) extends Event
  case class LarParsedErrors(errors: LarParsingError) extends Event
  case class GetStatePaginated(page: Int)

  def props(id: SubmissionId): Props = Props(new HmdaFileParser(id))

  def createHmdaFileParser(system: ActorSystem, submissionId: SubmissionId): ActorRef = {
    system.actorOf(HmdaFileParser.props(submissionId))
  }

  case class PaginatedFileParseState(tsParsingErrors: Seq[String], larParsingErrors: Seq[LarParsingError], totalErroredLines: Int)

  case class HmdaFileParseState(size: Int = 0, tsParsingErrors: Seq[String] = Nil, larParsingErrors: Seq[LarParsingError] = Nil) {
    def updated(event: Event): HmdaFileParseState = event match {
      case TsParsed(_) | LarParsed(_) =>
        HmdaFileParseState(size + 1, tsParsingErrors, larParsingErrors)
      case TsParsedErrors(errors) =>
        HmdaFileParseState(size, tsParsingErrors ++ errors, larParsingErrors)
      case LarParsedErrors(errors) =>
        HmdaFileParseState(size, tsParsingErrors, larParsingErrors :+ errors)
    }
  }

}

class HmdaFileParser(submissionId: SubmissionId) extends HmdaPersistentActor {

  import HmdaFileParser._

  var state = HmdaFileParseState()
  var encounteredParsingErrors: Boolean = false

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def persistenceId: String = s"$name-$submissionId"

  val duration = 10.seconds
  implicit val timeout = Timeout(duration)

  val config = ConfigFactory.load()
  val flowParallelism = config.getInt("hmda.actor-flow-parallelism")

  override def receiveCommand: Receive = {

    case ReadHmdaRawFile(persistenceId, replyTo: ActorRef) =>

      val parsedTs = events(persistenceId)
        .filter { x => x.isInstanceOf[LineAdded] }
        .map { case LineAdded(_, data) => data }
        .take(1)
        .map(line => TsCsvParser(line))
        .map {
          case Left(errors) =>
            encounteredParsingErrors = true
            TsParsedErrors(errors)
          case Right(ts) => TsParsed(ts)
        }

      parsedTs
        .runForeach(pTs => self ! pTs)

      val parsedLar = events(persistenceId)
        .filter { x => x.isInstanceOf[LineAdded] }
        .map { case LineAdded(_, data) => data }
        .drop(1)
        .zip(Source.fromIterator(() => Iterator.from(2)))
        .map { case (lar, index) => LarCsvParser(lar, index) }
        .map {
          case Left(errors) =>
            encounteredParsingErrors = true
            LarParsedErrors(errors)
          case Right(lar) => LarParsed(lar)
        }

      parsedLar
        .mapAsync(parallelism = flowParallelism)(x => (self ? x).mapTo[Persisted.type])
        .runWith(Sink.actorRef(self, FinishParsing(replyTo)))

    case tp @ TsParsed(ts) =>
      persist(tp) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case tsErr @ TsParsedErrors(errors) =>
      persist(tsErr) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case lp @ LarParsed(lar) =>
      persist(lp) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
        sender() ! Persisted
      }

    case larErr @ LarParsedErrors(errors) =>
      persist(larErr) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
        sender() ! Persisted
      }

    case FinishParsing(replyTo) =>
      if (encounteredParsingErrors)
        replyTo ! ParsingCompletedWithErrors(submissionId)
      else
        replyTo ! ParsingCompleted(submissionId)

    case GetState =>
      sender() ! state

    case GetStatePaginated(page) =>
      val tsErrState = state.tsParsingErrors
      val tsLineError = if (tsErrState.isEmpty) 0 else 1
      val tsErrorsReturn = if (page == 1) tsErrState else Seq()

      val totalLarErrors: Int = state.larParsingErrors.size
      val p = PaginatedResource(totalLarErrors, tsLineError)(page)
      val larErrorsReturn = state.larParsingErrors.slice(p.fromIndex, p.toIndex)

      sender() ! PaginatedFileParseState(tsErrorsReturn, larErrorsReturn, totalLarErrors + tsLineError)

    case Shutdown =>
      context stop self

  }

}

