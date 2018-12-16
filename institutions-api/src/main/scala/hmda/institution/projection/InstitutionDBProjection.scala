package hmda.institution.projection

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionConverter
import hmda.institution.query.{InstitutionComponent, InstitutionEmailEntity}
import hmda.messages.institution.InstitutionEvents.{InstitutionCreated, InstitutionDeleted, InstitutionEvent, InstitutionModified}
import hmda.model.institution.Institution
import hmda.query.DbConfiguration._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

sealed trait InstitutionProjectionCommand
case class ProjectEvent(evt: InstitutionEvent) extends InstitutionProjectionCommand

object InstitutionDBProjection
    extends InstitutionComponent {

  val config = ConfigFactory.load()

  val name = "InstitutionDBProjector"

  implicit val institutionRepository = new InstitutionRepository(dbConfig)
  implicit val institutionEmailsRepository = new InstitutionEmailsRepository(
    dbConfig)

  implicit val ec: ExecutionContext = ExecutionContext.global

  val behavior: Behavior[InstitutionProjectionCommand] =
    Behaviors.setup { ctx =>
      val log = ctx.log
      val decider: Supervision.Decider = {
        case e: Throwable =>
          log.error(e.getLocalizedMessage)
          Supervision.Resume
      }
      implicit val system = ctx.system.toUntyped
      implicit val materializer = ActorMaterializer(
        ActorMaterializerSettings(system).withSupervisionStrategy(decider))

      log.info(s"Started $name")

      Behaviors.receiveMessage {
        case ProjectEvent(evt) =>
          log.info(s"Projecting event to Postgres: $evt")
          projectEvent(evt)
          Behaviors.same
        case _ =>
          Behaviors.ignore
      }
    }

  def projectEvent(event: InstitutionEvent): InstitutionEvent = {
    event match {
      case InstitutionCreated(i) =>
        updateTables(i)

      case InstitutionModified(i) =>
        updateTables(i)

      case InstitutionDeleted(lei) =>
        institutionRepository.deleteById(lei)
        deleteEmails(lei)
    }
    event
  }

  private def updateTables(inst: Institution): Future[List[Int]] = {
    val insertResult =
      institutionRepository.insertOrUpdate(InstitutionConverter.convert(inst))
    val emails = InstitutionConverter.emailsFromInstitution(inst).toList
    for {
      institutionRow <- insertResult
      emailsRows <- updateEmailsInSerial(emails)
    } yield emailsRows :+ institutionRow
  }

  private def updateEmailsInSerial(
      emails: List[InstitutionEmailEntity]): Future[List[Int]] = {
    emails.foldLeft(Future(List.empty[Int])) { (previousInserts, nextEmail) =>
      for {
        completedInserts <- previousInserts
        insertResult <- updateEmails(nextEmail)
      } yield completedInserts :+ insertResult
    }
  }

}
