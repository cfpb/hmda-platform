package hmda.submissionerrors.streams

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.persistence.query.EventEnvelope
import akka.stream.scaladsl.{ Sink, Source }
import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.filing.submission.SubmissionId
import hmda.query.HmdaQuery
import monix.eval.Task

import scala.concurrent.Future

/**
 * Step 2: Obtain all the errors for a given submission ID (LEI + Period + Sequence Number)
 */
object ErrorInformation {
  type EditName   = String
  type LineNumber = Long

  def obtainSubmissionErrors(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Task[Map[LineNumber, Set[EditName]]] =
    Task.fromFuture(submissionRowError(submissionId).runWith(collectErrors))

  def submissionRowError(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Source[HmdaRowValidatedError, NotUsed] = {
    val persistenceId = s"HmdaValidationError-$submissionId"
    HmdaQuery
      .currentEventEnvelopeByPersistenceId(persistenceId)
      .collect {
        case EventEnvelope(_, _, _, event: HmdaRowValidatedError) => event
      }
  }

  val collectErrors: Sink[HmdaRowValidatedError, Future[Map[LineNumber, Set[EditName]]]] =
    Sink.fold[Map[LineNumber, Set[EditName]], HmdaRowValidatedError](Map.empty[LineNumber, Set[EditName]]) { (acc, next) =>
      val lineNumber = next.rowNumber.toLong
      val editNames  = next.validationErrors.map(_.editName)
      val existing   = acc.getOrElse(lineNumber, Set.empty[EditName])
      val updated    = existing ++ editNames
      acc + (lineNumber -> updated)
    }
}