package hmda.api.processing.submission

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.PersistentActor
import hmda.api.model.processing.{ Institution, Institutions }
import hmda.api.processing.CommonMessages
import CommonMessages._
import hmda.api.processing.submission.InstitutionsFiling._

object InstitutionsFiling {

  case class CreateInstitution(i: Institution) extends Command
  case class ModifyInstitution(i: Institution) extends Command
  case class GetInstitutionByIdAndPeriod(fid: String, period: String) extends Command

  case class InstitutionCreated(i: Institution) extends Event
  case class InstitutionModified(i: Institution) extends Event

  def props: Props = Props(new InstitutionsFiling)

  def createInstitutionsFiling(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionsFiling.props, "institutions")
  }

  case class InstitutionsFilingState(institutions: Set[Institution] = Set.empty[Institution]) {
    def updated(event: Event): InstitutionsFilingState = {
      event match {
        case InstitutionCreated(i) =>
          InstitutionsFilingState(institutions + i)
        case InstitutionModified(i) =>
          val x = institutions.find(x => x.id == i.id).getOrElse(Institution())
          InstitutionsFilingState((institutions - x) + i)
      }
    }
  }
}

class InstitutionsFiling extends PersistentActor with ActorLogging {

  var state = InstitutionsFilingState()

  def updateState(event: Event) =
    state = state.updated(event)

  override def preStart(): Unit = {
    log.info(s"Institutions Filing started at ${self.path}")
  }

  override val persistenceId: String = "institutions"

  override def receiveCommand: Receive = {

    case CreateInstitution(i) =>
      if (!state.institutions.contains(i)) {
        persist(InstitutionCreated(i)) { e =>
          log.info(s"Persisted: $i")
          updateState(e)
        }
      }

    case ModifyInstitution(i) =>
      if (state.institutions.map(i => i.id).contains(i.id)) {
        persist(InstitutionModified(i)) { e =>
          log.info(s"Modified: ${i.name}")
          updateState(e)
        }
      }

    case GetInstitutionByIdAndPeriod(fid, period) =>
      val institution = state.institutions.find(x => x.id == fid && x.period == period).getOrElse(Institution())
      sender() ! institution

    case GetState =>
      sender() ! Institutions(state.institutions)

    case Shutdown => context stop self
  }

  override def receiveRecover: Receive = {
    case e: Event => updateState(e)
  }
}
