package hmda.persistence.messages.commands.institutions

import akka.actor.ActorRef
import hmda.model.institution.HmdaFiler
import hmda.persistence.messages.CommonMessages.Command

object HmdaFilerCommands {

  case class CreateHmdaFiler(hmdaFiler: HmdaFiler) extends Command
  case class CreateHmdaFilerWithReply(hmdaFiler: HmdaFiler, replyTo: ActorRef) extends Command
  case class DeleteHmdaFiler(hmdaFiler: HmdaFiler) extends Command
  case class FindHmdaFiler(institutionId: String) extends Command
  case class FindHmdaFilers(period: String) extends Command
}
