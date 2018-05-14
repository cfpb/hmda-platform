package hmda.persistence.institutions

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import hmda.model.institution.Institution

object InstitutionPersistence {

  sealed trait InstitutionCommand
  sealed trait InstitutionEvent
  case class CreateInstitution(i: Institution) extends InstitutionCommand
  case class InstitutionCreated(i: Institution) extends InstitutionEvent

  def behavior(): Behavior[InstitutionCommand] =
    Behaviors.receive[InstitutionCommand] {
      case (ctx, CreateInstitution(i)) =>
        val created = InstitutionCreated(i)
        println(s"Institution Created: $created")
        Behaviors.same
    }

}
