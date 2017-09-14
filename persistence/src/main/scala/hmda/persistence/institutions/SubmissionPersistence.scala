package hmda.persistence.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi._
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.institutions.SubmissionPersistence._
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.messages.events.institutions.SubmissionEvents._

object SubmissionPersistence {

  val name = "submissions"

  case object CreateSubmission extends Command
  case class UpdateSubmissionStatus(id: SubmissionId, status: SubmissionStatus) extends Command
  case class AddSubmissionFileName(id: SubmissionId, fileName: String) extends Command
  case class GetSubmissionById(id: SubmissionId) extends Command
  case object GetLatestSubmission extends Command

  def props(institutionId: String, period: String): Props = Props(new SubmissionPersistence(institutionId, period))

  def createSubmissions(institutionId: String, period: String, system: ActorSystem): ActorRef = {
    system.actorOf(SubmissionPersistence.props(institutionId, period).withDispatcher("persistence-dispatcher"))
  }

  case class SubmissionState(submissions: Seq[Submission] = Nil) {
    def updated(event: Event): SubmissionState = {
      event match {
        case SubmissionCreated(s) =>
          SubmissionState(s +: submissions)

        case SubmissionStatusUpdated(id, status) =>
          val x = submissions.find(x => x.id == id).getOrElse(Submission())
          val i = submissions.indexOf(x)

          val updatedSub: Submission = {
            if (status == Signed) {
              val now = System.currentTimeMillis
              x.copy(status = status, end = now, receipt = generateReceipt(x.id, now))
            } else x.copy(status = status)
          }

          SubmissionState(submissions.updated(i, updatedSub))

        case SubmissionFileNameAdded(id, fileName) =>
          val sub = submissions.find(_.id == id).getOrElse(Submission())
          val index = submissions.indexOf(sub)
          val updated = sub.copy(fileName = fileName)
          SubmissionState(submissions.updated(index, updated))
      }
    }

    private def generateReceipt(submissionId: SubmissionId, timestamp: Long): String = {
      s"$submissionId-$timestamp"
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
      val newSubmission = Submission(submissionId, Created, System.currentTimeMillis(), 0L)
      persist(SubmissionCreated(newSubmission)) { e =>
        updateState(e)
        log.debug(s"Submission Created with Submission Id: ${submissionId.toString}")
        sender() ! Some(newSubmission)
      }

    case UpdateSubmissionStatus(id, status) =>
      if (state.submissions.map(x => x.id).contains(id)) {
        persist(SubmissionStatusUpdated(id, status)) { e =>
          updateState(e)
          sender() ! Some(Submission(id, status))
        }
      } else {
        log.warning(s"Submission does not exist. Could not update submission with id $id")
        sender() ! None
      }

    case AddSubmissionFileName(id, fileName) =>
      if (state.submissions.map(_.id).contains(id)) {
        persist(SubmissionFileNameAdded(id, fileName)) { e =>
          updateState(e)
          sender() ! Some(Submission(id, fileName = fileName))
        }
      } else {
        log.warning(s"Submission does not exist. Could not add filename for submission with id $id")
        sender() ! None
      }

    case GetSubmissionById(id) =>
      val submission = state.submissions.find(s => s.id == id).getOrElse(Submission(SubmissionId(), Failed("No submission found"), 0L, 0L))
      sender() ! submission

    case GetLatestSubmission =>
      val latest = state.submissions.headOption.getOrElse(Submission(SubmissionId(), Failed("No submission found"), 0L, 0L))
      sender() ! latest

    case GetState =>
      sender() ! state.submissions.sortWith(_.id.sequenceNumber > _.id.sequenceNumber)

  }

}
