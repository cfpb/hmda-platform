package hmda.query.projections.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.pipe
import hmda.persistence.messages.CommonMessages.Command
import hmda.persistence.messages.events.institutions.InstitutionEvents._
import hmda.persistence.model.HmdaActor
import hmda.query.DbConfiguration._
import hmda.query.repository.institutions.InstitutionComponent

import scala.concurrent.ExecutionContext

object InstitutionDBProjection extends InstitutionComponent {

  val repository = new InstitutionRepository(config)

  case object CreateSchema extends Command
  case object DeleteSchema extends Command
  case class InstitutionInserted(n: Int)
  case class InstitutionUpdated(n: Int)

  def props(): Props = Props(new InstitutionDBProjection())

  def createInstitutionDBProjection(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionDBProjection.props())
  }

}

class InstitutionDBProjection extends HmdaActor {

  implicit val ec: ExecutionContext = context.dispatcher

  import hmda.query.repository.institutions.InstitutionConverter._
  import hmda.query.projections.institutions.InstitutionDBProjection._

  override def receive: Receive = {
    case CreateSchema =>
      repository.createSchema().map(_ => InstitutionSchemaCreated()) pipeTo sender()

    case DeleteSchema =>
      repository.dropSchema().map(_ => InstitutionSchemaDeleted()) pipeTo sender()

    case event: InstitutionEvent => event match {
      case InstitutionCreated(i) =>
        val query = toInstitutionQuery(i)
        log.debug(s"Created: $query")
        repository.insertOrUpdate(query)
          .map { x =>
            InstitutionInserted(x)
          } pipeTo sender()

      case InstitutionModified(i) =>
        val query = toInstitutionQuery(i)
        log.debug(s"Modified: $query")
        repository.update(query)
          .map(x => InstitutionUpdated(x)) pipeTo sender()
    }

  }

}
