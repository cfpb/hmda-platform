
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

/**
 * Step 1: Obtain all the submissions for a given LEI and Period
 */
object Submissions {
  implicit val setOrdering: Ordering[Submission] = (x: Submission, y: Submission) => x.id.toString compareTo y.id.toString

  def obtainSubmissions(lei: String, period: Period)(implicit system: ActorSystem[_]): Task[SortedSet[Submission]] =
    Task.fromFuture(submissions(lei, period).runWith(collectSubmissions))

  def submissions(lei: String, period: Period)(implicit system: ActorSystem[_]): Source[Submission, NotUsed] =
    HmdaQuery
      .currentEventEnvelopeByPersistenceId(s"Filing-$lei-$period")
      .collect {
        case EventEnvelope(_, _, _, evt: SubmissionAdded)   => evt.submission
        case EventEnvelope(_, _, _, evt: SubmissionUpdated) => evt.submission
      }

  val collectSubmissions: Sink[Submission, Future[SortedSet[Submission]]] =
    Sink.fold[SortedSet[Submission], Submission](SortedSet.empty[Submission](setOrdering)) { (acc, next) =>
      if (next.status.code >= 10) acc + next
      else acc
    }
}