package hmda.persistence.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.institution.Institution
import hmda.persistence.institutions.InstitutionPersistence._
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.institutions.InstitutionCommands._
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.HmdaPersistentActor

object InstitutionPersistence {

  val name = "institutions"

  def props: Props = Props(new InstitutionPersistence)

  def createInstitutions(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionPersistence.props.withDispatcher("persistence-dispatcher"), "institutions")
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

    case GetInstitutionById(id) =>
      val i = state.institutions.find(x => x.id == id)
      sender() ! i

    case GetInstitutionsById(ids) =>
      val institutions = state.institutions.filter(i => ids.contains(i.id))
      sender() ! institutions

    case GetInstitutionByRespondentId(respondentId) =>
      val institution = state.institutions.find { i =>
        i.respondent.externalId.value == respondentId
      }.getOrElse(Institution.empty)
      sender() ! institution

    case FindInstitutionByDomain(domain) =>
      if (domain.isEmpty) {
        sender() ! Set.empty[Institution]
      } else {
        sender() ! state.institutions.filter(i => i.emailDomains.map(e => extractDomain(e)).contains(domain.toLowerCase))
      }

    case GetState =>
      sender() ! state.institutions

    case Shutdown => context stop self
  }

  private def extractDomain(email: String): String = {
    val parts = email.toLowerCase.split("@")
    if (parts.length > 1)
      parts(1)
    else
      parts(0)
  }

}
