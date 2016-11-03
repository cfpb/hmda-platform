package hmda.query.projections.institutions

import akka.actor.Props
import hmda.persistence.messages.events.institutions.InstitutionEvents.{InstitutionCreated, InstitutionEvent, InstitutionModified}
import hmda.persistence.model.HmdaActor
import hmda.query.dao.institutions.Institutions
import slick.lifted.TableQuery

object InstitutionDBProjection {
  def props(): Props = Props(new InstitutionDBProjection)
}

class InstitutionDBProjection extends HmdaActor {

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
