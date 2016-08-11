package hmda.persistence.institutions

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import hmda.model.institution.Institution
import hmda.persistence.CommonMessages._
import hmda.persistence.institutions.InstitutionPersistence._

object InstitutionPersistence {

  case class CreateInstitution(i: Institution) extends Command
  case class ModifyInstitution(i: Institution) extends Command
  case class GetInstitutionById(institutionId: String) extends Command

  case class InstitutionCreated(i: Institution) extends Event
  case class InstitutionModified(i: Institution) extends Event

  def props: Props = Props(new InstitutionPersistence)

  def createInstitutions(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionPersistence.props, "institutions")
  }

  case class InstitutionsState(institutions: Set[Institution] = Set.empty[Institution]) {
    def updated(event: Event): InstitutionsState = {
      event match {
        case InstitutionCreated(i) =>
          InstitutionsState(institutions + i)
        case InstitutionModified(i) =>
          val others = institutions.filterNot(_.id == i.id)
          InstitutionsState(others + i)
      }
    }
  }
}

class InstitutionPersistence extends PersistentActor with ActorLogging {

  var state = InstitutionsState()

  def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def preStart(): Unit = {
    log.debug(s"Institutions started at ${self.path}")
  }

  override def persistenceId: String = "institutions"

  override def receiveRecover: Receive = {
    case e: Event => updateState(e)
    case SnapshotOffer(_, snapshot: InstitutionsState) =>
      log.debug("Recovering from snapshot")
      state = snapshot
  }

  override def receiveCommand: Receive = {
    case CreateInstitution(i) =>
      if (!state.institutions.contains(i)) {
        persist(InstitutionCreated(i)) { e =>
          log.debug(s"Persisted: $i")
          updateState(e)
        }
      } else {
        log.warning(s"Institution already exists. Could not create $i")
      }

    case ModifyInstitution(i) =>
      if (state.institutions.map(i => i.id).contains(i.id)) {
        persist(InstitutionModified(i)) { e =>
          log.debug(s"Modified: ${i.name}")
          updateState(e)
        }
      } else {
        log.warning(s"Institution does not exist. Could not update $i")
      }

    case GetInstitutionById(institutionId) =>
      val institution = state.institutions.find(x => x.id.toString == institutionId)
      sender() ! institution

    case GetState =>
      sender() ! state.institutions

    case Shutdown => context stop self
  }

}
