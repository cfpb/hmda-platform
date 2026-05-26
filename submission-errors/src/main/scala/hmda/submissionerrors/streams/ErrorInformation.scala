package hmda.submissionerrors.streams

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.persistence.query.EventEnvelope
import akka.stream.scaladsl.{ Sink, Source }
import hmda.messages.submission.SubmissionProcessingEvents.{ HmdaMacroValidatedError, HmdaRowValidatedError }
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
  type Error = Either[HmdaRowValidatedError, HmdaMacroValidatedError]

  /**
   * This is responsible for fetching set of `HmdaRowValidatedError` for a given submission
   * For example:
   *
   * @param submissionId is the submission ID
   * @param system is the actor system needed to run the Akka Stream
   * @return
   */
  def obtainSubmissionErrors(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Task[Set[Error]] =
    Task.fromFuture(submissionErrors(submissionId).runWith(collectErrors))

  private[streams] def submissionErrors(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Source[Error, NotUsed] = {
    val persistenceId = s"HmdaValidationError-$submissionId"
    HmdaQuery
      .currentEventEnvelopeByPersistenceId(persistenceId)
      .collect {
        case EventEnvelope(_, _, _, event: HmdaRowValidatedError) => Left(event)
        case EventEnvelope(_, _, _, event: HmdaMacroValidatedError) => Right(event)
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
   *    Set(
   *      HmdaRowValidatedError(line number = 1, edit names = Set(EditName1, EditName2),
   *      HmdaRowValidatedError(line number = 4, edit names = Set(EditName1, EditName3)
   *    )
   * )
   */
  private[streams] val collectErrors: Sink[Error, Future[Set[Error]]] = {
    Sink.fold[Set[Error], Error](Set.empty[Error])((acc, ele) => acc + ele)
  }
}
// $COVERAGE-ON$