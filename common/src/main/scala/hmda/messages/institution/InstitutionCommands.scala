package hmda.messages.institution

import akka.actor.typed.ActorRef
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionEvent
}
import hmda.model.institution.Institution

object InstitutionCommands {
  sealed trait InstitutionCommand

  case class CreateInstitution(i: Institution,
                               replyTo: ActorRef[InstitutionCreated])
      extends InstitutionCommand

  case class ModifyInstitution(i: Institution,
                               replyTo: ActorRef[InstitutionEvent])
      extends InstitutionCommand

  case class DeleteInstitution(LEI: String, replyTo: ActorRef[InstitutionEvent])
      extends InstitutionCommand

  case class GetInstitution(replyTo: ActorRef[Option[Institution]])
      extends InstitutionCommand

  case object InstitutionStop extends InstitutionCommand
}
