package hmda.messages.institution

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionEvent
}
import hmda.model.institution.Institution

object InstitutionCommands {
  sealed trait InstitutionCommand extends Command

  final case class CreateInstitution(i: Institution,
                                     replyTo: ActorRef[InstitutionCreated])
      extends InstitutionCommand

  final case class ModifyInstitution(i: Institution,
                                     replyTo: ActorRef[InstitutionEvent])
      extends InstitutionCommand

  final case class DeleteInstitution(LEI: String,
                                     replyTo: ActorRef[InstitutionEvent])
      extends InstitutionCommand

  final case class GetInstitution(replyTo: ActorRef[Option[Institution]])
      extends InstitutionCommand

  final case object InstitutionStop extends InstitutionCommand
}
