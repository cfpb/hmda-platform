package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{Cluster, Join}
import akka.stream.Materializer
import akka.stream.scaladsl.StreamConverters
import akka.util.Timeout
import hmda.messages.submission.HmdaRawDataCommands.AddLines
import hmda.messages.submission.HmdaRawDataReplies.LinesAdded
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.messages.submission.ValidationProgressTrackerCommands
import hmda.messages.submission.ValidationProgressTrackerCommands.ValidationProgressTrackerCommand
import hmda.model.filing.submission.{MacroErrors, SubmissionId, Verified}
import hmda.model.processing.state.{EditSummary, HmdaValidationErrorState, ValidationProgress, ValidationProgressTrackerState}
import hmda.model.validation._
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.institution.InstitutionPersistence
import hmda.util.streams.FlowUtils.framing
import hmda.utils.YearUtils.Period
import hmda.validation.rules.lar.syntactical.S304
import org.scalatest.concurrent.ScalaFutures

import java.time.Instant
import scala.concurrent.duration._

class HmdaValidationErrorSpec extends AkkaCassandraPersistenceSpec with ScalaFutures {
  override implicit val system      = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)
  SubmissionManager.startShardRegion(sharding)
  SubmissionPersistence.startShardRegion(sharding)
  HmdaValidationError.startShardRegion(sharding)
  EditDetailsPersistence.startShardRegion(sharding)
  InstitutionPersistence.startShardRegion(sharding)
  HmdaRawData.startShardRegion(sharding)

  val errorsProbe = TestProbe[HmdaRowValidatedError]("processing-event")
  val stateProbe  = TestProbe[HmdaValidationErrorState]("state-probe")
  val eventsProbe = TestProbe[SubmissionProcessingEvent]("events-probe")
  val signedProbe = TestProbe[SubmissionSignedEvent]("sign-event")
  val trackerRefProbe = TestProbe[ActorRef[ValidationProgressTrackerCommand]]("tracker-ref-probe")
  val trackerProbe = TestProbe[ValidationProgressTrackerState]("tracker-probe")

  "Validation Errors" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    def selectHmdaValidationError(submissionId: SubmissionId) = sharding.entityRefFor(HmdaValidationError.typeKey, s"${HmdaValidationError.name}-${submissionId.toString}")

    "track syntactical validity progress" in {
      // given - populating test data for the submission
      val submissionId = SubmissionId("8123912", Period(2018, None), 1)
      val hmdaValidationError = selectHmdaValidationError(submissionId)
      val hmdaRawData = sharding.entityRefFor(HmdaRawData.typeKey, s"${HmdaRawData.name}-${submissionId.toString}")

      val patience = 30.seconds
      implicit val timeout: Timeout = Timeout(patience)
      implicit val mat: Materializer = Materializer(system)

      StreamConverters
        .fromInputStream(() => getClass.getResourceAsStream("/error_test_files/trigger_s304_s305.txt"))
        .via(framing("\n"))
        .mapAsync(1)(data => hmdaRawData ? ((ref: ActorRef[LinesAdded]) =>
          AddLines(submissionId, Instant.now.toEpochMilli, List(data.utf8String), Some(ref))))
        .run()
        .futureValue

      import ValidationProgress._

      // subscribe to progress updates; we expect an initial progress message, where no validation stage has yet started
      hmdaValidationError ! TrackProgress(trackerRefProbe.ref)
      val tracker = trackerRefProbe.expectMessageType[ActorRef[ValidationProgressTrackerCommand]]
      tracker ! ValidationProgressTrackerCommands.Subscribe(trackerProbe.ref)
      trackerProbe.expectMessage(ValidationProgressTrackerState(Waiting, Set(), Waiting, Set(), Waiting, Set()))

      // when
      hmdaValidationError ! StartSyntacticalValidity(submissionId)

      // then
      trackerProbe.expectMessage(patience, ValidationProgressTrackerState(InProgress(1), Set(), Waiting, Set(), Waiting, Set()))
      trackerProbe.expectMessage(patience, ValidationProgressTrackerState(InProgress(25), Set(), Waiting, Set(), Waiting, Set()))
      trackerProbe.expectMessage(patience, ValidationProgressTrackerState(InProgress(50), Set(), Waiting, Set(), Waiting, Set()))
      trackerProbe.expectMessage(patience, ValidationProgressTrackerState(InProgress(90), Set(S304.name), Waiting, Set(), Waiting, Set()))
      trackerProbe.expectMessage(patience, ValidationProgressTrackerState(InProgress(95), Set(S304.name), Waiting, Set(), Waiting, Set()))
      trackerProbe.expectMessage(patience, ValidationProgressTrackerState(InProgress(99), Set(S304.name), Waiting, Set(), Waiting, Set()))
      trackerProbe.expectMessage(patience, ValidationProgressTrackerState(CompletedWithErrors, Set(S304.name), Waiting, Set(), Waiting, Set()))
    }

    "be persisted and read back" in {
      val submissionId = SubmissionId("12345", Period(2018, None), 1)
      val hmdaValidationError = selectHmdaValidationError(submissionId)

      val tsError: ValidationError =
        SyntacticalValidationError("12345XXX", "S300", TsValidationError)
      val larErrors: Seq[ValidationError] = Seq(
        SyntacticalValidationError("12345XXX", "S300", LarValidationError),
        SyntacticalValidationError("12345XXX", "S301", LarValidationError),
        ValidityValidationError("", "V600", LarValidationError),
        QualityValidationError("12345XXX", "Q601"),
        MacroValidationError("Q634")
      )
      hmdaValidationError ! PersistHmdaRowValidatedError(submissionId, 1, List(tsError), Some(errorsProbe.ref))
      errorsProbe.expectMessage(HmdaRowValidatedError(1, List(tsError)))
      val larErrorsWithIndex = Iterator.from(2).zip(larErrors.toIterator)
      larErrorsWithIndex.foreach { errorWithIndex =>
        val index = errorWithIndex._1
        val error = errorWithIndex._2
        hmdaValidationError ! PersistHmdaRowValidatedError(submissionId, index, List(error), Some(errorsProbe.ref))
        errorsProbe.expectMessage(HmdaRowValidatedError(index, List(error)))
      }
      hmdaValidationError ! GetHmdaValidationErrorState(submissionId, stateProbe.ref)

      val syntacticalEditSummary =
        Set(
          EditSummary(
            "S300",
            Syntactical,
            TsValidationError
          ),
          EditSummary(
            "S300",
            Syntactical,
            LarValidationError
          ),
          EditSummary(
            "S301",
            Syntactical,
            LarValidationError
          )
        )
      val validityEditSummary =
        Set(
          EditSummary(
            "V600",
            Validity,
            LarValidationError
          )
        )
      val qualityEditSummary =
        Set(
          EditSummary(
            "Q601",
            Quality,
            LarValidationError
          )
        )
      val macroEditSummary =
        Set(
          EditSummary(
            "Q634",
            hmda.model.validation.Macro,
            LarValidationError
          )
        )

      stateProbe.expectMessage(
        HmdaValidationErrorState(
          1,
          syntacticalEditSummary,
          validityEditSummary,
          qualityEditSummary,
          macroEditSummary,
          qualityVerified = false
        )
      )
    }

    "finish validation, verify and sign" in {
      val submissionId = SubmissionId("12345", Period(2018, None), 1)
      val hmdaValidationError = selectHmdaValidationError(submissionId)

      val email = "bank@bankaddress.com"
      val username = "some-user"

      hmdaValidationError ! VerifyQuality(submissionId, true, eventsProbe.ref)
      eventsProbe.expectMessage(NotReadyToBeVerified(submissionId))
      hmdaValidationError ! CompleteQuality(submissionId)

      hmdaValidationError ! SignSubmission(submissionId, signedProbe.ref, email, username)
      signedProbe.expectMessage(SubmissionNotReadyToBeSigned(submissionId))

      hmdaValidationError ! VerifyQuality(submissionId, true, eventsProbe.ref)
      eventsProbe.expectMessage(QualityVerified(submissionId, true, MacroErrors))

      hmdaValidationError ! VerifyMacro(submissionId, true, eventsProbe.ref)
      eventsProbe.expectMessage(MacroVerified(submissionId, true, Verified))

      hmdaValidationError ! SignSubmission(submissionId, signedProbe.ref, email, username)
      signedProbe.expectMessageType[SubmissionSigned]
    }
  }

}