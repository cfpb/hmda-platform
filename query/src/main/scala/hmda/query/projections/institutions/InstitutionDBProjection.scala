package hmda.query.projections.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.pipe
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionEvent, InstitutionModified }
import hmda.persistence.model.HmdaActor
import hmda.query.repository.institutions.InstitutionsRepository

import scala.concurrent.ExecutionContext

object InstitutionDBProjection {
  case class InstitutionInserted(n: Int)
  case class InstitutionUpdated(n: Int)
  def props(repository: InstitutionsRepository): Props = Props(new InstitutionDBProjection(repository))

  def createInstitutionDBProjection(system: ActorSystem, repository: InstitutionsRepository): ActorRef = {
    system.actorOf(InstitutionDBProjection.props(repository))
  }

}

class InstitutionDBProjection(repository: InstitutionsRepository) extends HmdaActor {

  implicit val ec: ExecutionContext = context.dispatcher

  import hmda.query.repository.institutions.InstitutionConverter._
  import hmda.query.projections.institutions.InstitutionDBProjection._

  override def receive: Receive = {
    case event: InstitutionEvent => event match {
      case InstitutionCreated(i) =>
        val query = toInstitutionQuery(i)
        log.debug(s"Created: $query")
        repository.insertOrUpdate(query)
          .map(x => InstitutionInserted(x)) pipeTo sender()

      case InstitutionModified(i) =>
        val query = toInstitutionQuery(i)
        log.info(s"Modified: $query")
        repository.update(query)
          .map(x => InstitutionUpdated(x)) pipeTo sender()
    }

  }

}
