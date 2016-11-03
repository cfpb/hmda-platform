package hmda.query.projections.institutions

import akka.actor.Props
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionEvent, InstitutionModified }
import hmda.persistence.model.HmdaActor
import hmda.query.sql.institutions.Institutions
import slick.lifted.TableQuery

object InstitutionQueryProjector {
  def props(): Props = Props(new InstitutionQueryProjector)
}

class InstitutionQueryProjector extends HmdaActor {

  val institutions = TableQuery[Institutions]

  override def receive: Receive = {
    case event: InstitutionEvent => event match {
      case InstitutionCreated(i) =>
        log.debug(s"Created: $i")
      case InstitutionModified(i) =>
        log.debug(s"Modified: $i")
    }
  }
}
