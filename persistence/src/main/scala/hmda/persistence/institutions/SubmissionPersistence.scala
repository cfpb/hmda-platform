package hmda.persistence.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.{ Created, Submission, SubmissionId, SubmissionStatus }
import hmda.persistence.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.HmdaPersistentActor
import hmda.persistence.institutions.SubmissionPersistence._

object SubmissionPersistence {

  val name = "submissions"

  case object CreateSubmission extends Command
  case class UpdateSubmissionStatus(id: SubmissionId, status: SubmissionStatus) extends Command
  case class GetSubmissionById(id: SubmissionId) extends Command
  case object GetLatestSubmission extends Command

  case class SubmissionCreated(submission: Submission) extends Event
  case class SubmissionStatusUpdated(id: SubmissionId, status: SubmissionStatus) extends Event

  def props(institutionId: String, period: String): Props = Props(new SubmissionPersistence(institutionId, period))

  def createSubmissions(institutionId: String, period: String, system: ActorSystem): ActorRef = {
    system.actorOf(SubmissionPersistence.props(institutionId, period))
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

//Submissions for an institution, per filing period
class SubmissionPersistence(institutionId: String, period: String) extends HmdaPersistentActor {

  var state = SubmissionState()

  override def updateState(e: Event): Unit = {
    state = state.updated(e)
  }

  override def persistenceId: String = s"submissions-$institutionId-$period"

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case CreateSubmission =>
      val seqNr = state.submissions.size + 1
      val submissionId = SubmissionId(institutionId, period, seqNr)
      val newSubmission = Submission(submissionId, Created)
      persist(SubmissionCreated(newSubmission)) { e =>
        updateState(e)
      }

    case UpdateSubmissionStatus(id, status) =>
      if (state.submissions.map(x => x.id).contains(id)) {
        persist(SubmissionStatusUpdated(id, status)) { e =>
          updateState(e)
        }
      } else {
        log.warning(s"Submission does not exist. Could not update submission with id $id")
      }

    case GetSubmissionById(id) =>
      val submission = state.submissions.find(s => s.id == id).getOrElse(Submission())
      sender() ! submission

    case GetLatestSubmission =>
      val latest = state.submissions.headOption.getOrElse(Submission())
      sender() ! latest

    case GetState =>
      sender() ! state.submissions

  }

}
