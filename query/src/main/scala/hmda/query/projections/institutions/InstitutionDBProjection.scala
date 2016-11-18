package hmda.query.projections.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.pipe
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionEvent, InstitutionModified }
import hmda.persistence.model.HmdaActor
import hmda.query.dao.institutions.InstitutionRepository
import hmda.query.dao.institutions.InstitutionConverter._
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext

object InstitutionDBProjection {
  case class InstitutionInserted(n: Int)
  case class InstitutionUpdated(n: Int)
  def props(): Props = Props(new InstitutionDBProjection)

  def createInstitutionDBProjection(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionDBProjection.props())
  }

}

class InstitutionDBProjection extends HmdaActor with InstitutionRepository with H2Driver {

  import InstitutionDBProjection._

  val repository = new InstitutionBaseRepository

  val db: Database = Database.forConfig("h2mem")

  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case event: InstitutionEvent => event match {
      case InstitutionCreated(i) =>
        val query = toInstitutionQuery(i)
        log.debug(s"Created: $query")
        repository.save(query)
          .map(x => InstitutionInserted(x)) pipeTo self

      case InstitutionModified(i) =>
        val query = toInstitutionQuery(i)
        log.info(s"Modified: $query")
        repository.update(query)
          .map(x => InstitutionUpdated(x)) pipeTo self

    }

    case InstitutionInserted(size) =>
      log.debug(s"Inserted: $size")

    case InstitutionUpdated(size) =>
      log.debug(s"Updated: $size")
  }

}
