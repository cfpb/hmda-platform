package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.submission.EditDetailsEvents.{ EditDetailsPersistenceEvent, EditDetailsRowCounted }
import hmda.model.edits.EditDetails
import hmda.model.filing.submission.SubmissionId

import scala.concurrent.Future

object EditDetailsCommands {

  trait EditDetailsPersistenceCommand

  case class PersistEditDetails(editDetails: EditDetails, replyTo: Option[ActorRef[EditDetailsPersistenceEvent]])
    extends EditDetailsPersistenceCommand

  case class GetEditRowCount(editName: String, replyTo: ActorRef[EditDetailsRowCounted]) extends EditDetailsPersistenceCommand

  case class GetEditDetails(submissionId: SubmissionId, editName: String, page: Int, replyTo: ActorRef[Future[Seq[EditDetails]]])
    extends EditDetailsPersistenceCommand

  case object StopEditDetails extends EditDetailsPersistenceCommand
}