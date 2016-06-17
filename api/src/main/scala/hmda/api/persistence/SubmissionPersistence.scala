package hmda.api.persistence

import akka.actor.{ActorLogging, ActorRef, ActorSystem, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import hmda.api.persistence.CommonMessages.{Command, Event}
import hmda.api.persistence.SubmissionPersistence._
import hmda.model.fi.{Submission, SubmissionStatus}

object SubmissionPersistence {

  case object CreateSubmission extends Command
  case class UpdateSubmissionStatus(id: String, status: SubmissionStatus) extends Command

  case class SubmissionCreated(submission: Submission) extends Event
  case class SubmissionStatusUpdated(id: String, status: SubmissionStatus) extends Event

  def props(fid: String, filingId: String): Props = Props(new SubmissionPersistence(fid, filingId))

  def createSubmissions(fid: String, filingId: String, system: ActorSystem): ActorRef = {
    system.actorOf(SubmissionPersistence.props(fid, filingId))
  }

  case class SubmissionState(submissions: Seq[Submission] = Nil) {
    def updated(event: Event): SubmissionState = {
      event match {
        case SubmissionCreated(s) =>
          SubmissionState(s +: submissions)
        case SubmissionStatusUpdated(id, status) =>
          val x = submissions.find(x => x.id == id).getOrElse(Submission())
          val i = submissions.indexOf(x)
          SubmissionState(submissions.updated(i, x.copy(submissionStatus = status)))
      }
    }
  }

}

//Submissions for an institution, per filing period, id should be <fid>-<filingId>-<submissionId>
//where submissionId is an integer that is incremented every time a new submission is added
class SubmissionPersistence(fid: String, filingId: String) extends PersistentActor with ActorLogging {

  var state = SubmissionState()

  def updateState(e: Event): Unit = {
    state = state.updated(e)
  }

  override def persistenceId: String = s"submissions-$fid-$filingId"

  override def receiveRecover: Receive = {
    case e: Event => updateState(e)
    case SnapshotOffer(_, snapshot: SubmissionState) =>
      log.info(s"Recovering from snapshot")
      state = snapshot
  }

  override def receiveCommand: Receive = {
    case CreateSubmission =>
      val index = state.submissions.size
      val newSubmission = Submission().copy(id = s"$fid-$filingId-$index")
      persist(SubmissionCreated(newSubmission)){ e =>
        updateState(e)
      }

    case UpdateSubmissionStatus(id, status) =>
      if (state.submissions.map(x =>x.id).contains(id)) {
        persist(SubmissionStatusUpdated(id, status)){e =>
          updateState(e)
        }
      }

  }

}
