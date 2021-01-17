package hmda.submissionerrors.streams

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.persistence.query.EventEnvelope
import akka.stream.scaladsl.{ Sink, Source }
import hmda.messages.filing.FilingEvents.{ SubmissionAdded, SubmissionUpdated }
import hmda.model.filing.submission.Submission
import hmda.query.HmdaQuery
import hmda.utils.YearUtils.Period
import monix.eval.Task

import scala.collection.SortedSet
import scala.concurrent.Future
// $COVERAGE-OFF$
/**
 * Step 1: Obtain all the submissions for a given LEI and Period
 */
object Submissions {

  /**
   * Obtain all the submissions for a given LEI and Period
   *
   * @param lei
   * @param period
   * @param system
   * @return
   */
  def obtainSubmissions(lei: String, period: Period)(implicit system: ActorSystem[_]): Task[SortedSet[Submission]] =
    Task.deferFuture(submissions(lei, period).runWith(collectSubmissions))

  /**
   * This provides a stream of submissions for a given LEI and Period
   * Note: A submission can be updated (i.e. the status of the submission can be updated)
   *
   * @param lei
   * @param period
   * @param system
   * @return
   */
  def submissions(lei: String, period: Period)(implicit system: ActorSystem[_]): Source[Submission, NotUsed] =
    HmdaQuery
      .currentEventEnvelopeByPersistenceId(s"Filing-$lei-$period")
      .collect {
        case EventEnvelope(_, _, _, evt: SubmissionAdded)   => evt.submission
        case EventEnvelope(_, _, _, evt: SubmissionUpdated) => evt.submission
      }

  /**
   * Define an Ordering for the SortedSet[Submission] which is used to determine uniqueness and also pick up
   *  the latest submission
   */
  private implicit val setOrdering: Ordering[Submission] = (x: Submission, y: Submission) => x.id.toString compareTo y.id.toString

  /**
   * val source = Source(
   *   Submission(code=1,  LEI=X, Period=2018, SequenceNumber=1),  // S1 v1.0 - gets filtered out because its the old version
   *   Submission(code=11, LEI=X, Period=2018, SequenceNumber=1),  // S1 v1.1
   *   Submission(code=11, LEI=Y, Period=2018, SequenceNumber=1),  // S2 v1.0
   *   Submission(code=2,  LEI=Z, Period=2018, SequenceNumber=1),  // S3 v1.0 - gets filtered out because code=2
   * )
   * val res = source.to(sink)
   * Future(
   *   SortedSet(
   *     Submission(code=11, LEI=X, Period=2018, SequenceNumber=1),
   *     Submission(code=11, LEI=Y, Period=2018, SequenceNumber=1)
   *   )
   * )
   */
  val collectSubmissions: Sink[Submission, Future[SortedSet[Submission]]] =
    Sink.fold[SortedSet[Submission], Submission](SortedSet.empty[Submission](setOrdering)) { (acc, next) =>
      if (next.status.code >= 10) acc + next
      else acc
    }
}
// $COVERAGE-ON$