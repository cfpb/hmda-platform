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
// $COVERAGE-OFF$
/**
 * Step 2: Obtain all the errors for a given submission ID (LEI + Period + Sequence Number)
 */
object ErrorInformation {
  type EditName   = String
  type LineNumber = Long

  /**
   * This is responsible for fetching the line numbers that have failed validation for a given submission
   * For example:
   * Map(
   *   line number 2  -> Set(Q614, Q617),
   *   line number 16 -> Set(Q609)
   * )
   *
   * @param submissionId is the submission ID
   * @param system is the actor system needed to run the Akka Stream
   * @return
   */
  def obtainSubmissionErrors(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Task[Map[LineNumber, Set[EditName]]] =
    Task.fromFuture(submissionRowError(submissionId).runWith(collectErrors))

  private[streams] def submissionRowError(
                                           submissionId: SubmissionId
                                         )(implicit system: ActorSystem[_]): Source[HmdaRowValidatedError, NotUsed] = {
    val persistenceId = s"HmdaValidationError-$submissionId"
    HmdaQuery
      .currentEventEnvelopeByPersistenceId(persistenceId)
      .collect {
        case EventEnvelope(_, _, _, event: HmdaRowValidatedError) => event
      }
  }

  /**
   * Here is an example (simplified HmdaRowValidatedError):
   * val source = Source(
   *  HmdaRowValidatedError(line number = 1, edit names = Set(EditName1, EditName2),
   *  HmdaRowValidatedError(line number = 4, edit names = Set(EditName1, EditName3)
   * )
   *
   * val res = source.to(collectErrors).run // Connecting source to sink to create a graph and run it
   *
   * We would expect the result of running the stream to be a
   * Future(
   *   Map(
   *      line number 1 -> Set(EditName1, EditName2),
   *      line number 4 -> Set(EditName1, EditName3)
   *    )
   * )
   */
  private[streams] val collectErrors: Sink[HmdaRowValidatedError, Future[Map[LineNumber, Set[EditName]]]] =
    Sink.fold[Map[LineNumber, Set[EditName]], HmdaRowValidatedError](Map.empty[LineNumber, Set[EditName]]) { (acc, next) =>
      val lineNumber = next.rowNumber.toLong
      val editNames  = next.validationErrors.map(_.editName)
      val existing   = acc.getOrElse(lineNumber, Set.empty[EditName])
      val updated    = existing ++ editNames
      acc + (lineNumber -> updated)
    }
}
// $COVERAGE-ON$