package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{Cluster, Join}
import hmda.messages.submission.SubmissionProcessingCommands.{
  GetHmdaValidationErrorState,
  PersistHmdaRowValidatedError
}
import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.filing.submission.SubmissionId
import hmda.model.processing.state.HmdaValidationErrorState
import hmda.model.validation._
import hmda.persistence.AkkaCassandraPersistenceSpec

class HmdaFileValidationSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val sharding = ClusterSharding(typedSystem)
  SubmissionManager.startShardRegion(sharding)
  SubmissionPersistence.startShardRegion(sharding)
  HmdaValidationError.startShardRegion(sharding)

  val submissionId = SubmissionId("12345", "2018", 1)

  val errorsProbe = TestProbe[HmdaRowValidatedError]("processing-event")
  val stateProbe = TestProbe[HmdaValidationErrorState]("state-probe")

  "Validation Errors" must {
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    "be persisted and read back" in {
      val hmdaValidationError = sharding.entityRefFor(
        HmdaValidationError.typeKey,
        s"${HmdaValidationError.name}-${submissionId.toString}")
      val tsError: ValidationError =
        SyntacticalValidationError("12345XXX", "S300", TsValidationError)
      val larErrors: Seq[ValidationError] = Seq(
        SyntacticalValidationError("12345XXX", "S300", LarValidationError),
        SyntacticalValidationError("12345XXX", "S301", LarValidationError),
        ValidityValidationError("", "V600", LarValidationError),
        QualityValidationError("12345XXX", "Q601")
      )
      hmdaValidationError ! PersistHmdaRowValidatedError(1,
                                                         List(tsError),
                                                         Some(errorsProbe.ref))
      errorsProbe.expectMessage(HmdaRowValidatedError(1, List(tsError)))
      val larErrorsWithIndex = Iterator.from(2).zip(larErrors.toIterator)
      larErrorsWithIndex.foreach { errorWithIndex =>
        val index = errorWithIndex._1
        val error = errorWithIndex._2
        hmdaValidationError ! PersistHmdaRowValidatedError(
          index,
          List(error),
          Some(errorsProbe.ref))
        errorsProbe.expectMessage(HmdaRowValidatedError(index, List(error)))
      }
//      hmdaValidationError ! GetHmdaValidationErrorState(submissionId,
//                                                        stateProbe.ref)
//      stateProbe.expectMessage(HmdaValidationErrorState(5, 3, 1, 1, 0))
    }
  }

}
