package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.submission.EditDetailPersistenceEvents.EditDetailsPersistenceEvent
import hmda.model.edits.EditDetails
import hmda.model.filing.submission.SubmissionId

import scala.concurrent.Future

object EditDetailPersistenceCommands {

  trait EditDetailsPersistenceCommand

  case class PersistEditDetails(
      editDetails: EditDetails,
      replyTo: Option[ActorRef[EditDetailsPersistenceEvent]])
      extends EditDetailsPersistenceCommand

  case class GetEditRowCount(editName: String, replyTo: ActorRef[Int])
      extends EditDetailsPersistenceCommand

  case class GetEditDetails(submissionId: SubmissionId,
                            editName: String,
                            page: Int,
                            replyTo: ActorRef[Future[Seq[EditDetails]]])
      extends EditDetailsPersistenceCommand
}
