package hmda.persistence.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.institution.Institution
import hmda.persistence.institutions.InstitutionPersistence._
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.HmdaPersistentActor

object InstitutionPersistence {

  val name = "institutions"

  case class CreateInstitution(i: Institution) extends Command
  case class ModifyInstitution(i: Institution) extends Command
  case class GetInstitution(id: String) extends Command

  def props: Props = Props(new InstitutionPersistence)

  def createInstitutions(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionPersistence.props, "institutions")
  }

  case class InstitutionPersistenceState(institutions: Set[Institution] = Set.empty[Institution]) {
    def updated(event: Event): InstitutionPersistenceState = {
      event match {
        case InstitutionCreated(i) =>
          InstitutionPersistenceState(institutions + i)
        case InstitutionModified(i) =>
          val elem = institutions.find(x => x.id == i.id).getOrElse(Institution.empty)
          val updated = (institutions - elem) + i
          InstitutionPersistenceState(updated)

      }
    }
  }
}

class InstitutionPersistence extends HmdaPersistentActor {

  var state = InstitutionPersistenceState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def persistenceId: String = s"$name"

  override def receiveCommand: Receive = {
    case CreateInstitution(i) =>
      if (!state.institutions.map(x => x.id).contains(i.id)) {
        persist(InstitutionCreated(i)) { e =>
          log.debug(s"Persisted: $i")
          updateState(e)
          sender() ! Some(e.institution)
        }
      } else {
        sender() ! None
        log.warning(s"Institution already exists. Could not create $i")
      }

    case ModifyInstitution(i) =>
      if (state.institutions.map(x => x.id).contains(i.id)) {
        persist(InstitutionModified(i)) { e =>
          log.debug(s"Modified: ${i.respondent.name}")
          updateState(e)
          sender() ! Some(e.institution)
        }
      } else {
        sender() ! None
        log.warning(s"Institution does not exist. Could not update $i")
      }

    case GetInstitution(id) =>
      val i = state.institutions.find(x => x.id == id)
      sender() ! i

    case GetState =>
      sender() ! state.institutions

    case Shutdown => context stop self
  }

}
