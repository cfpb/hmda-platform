package hmda.institution.projection

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionConverter
import hmda.institution.query._
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionEvent,
  InstitutionModified
}
import hmda.model.institution.Institution
import hmda.query.DbConfiguration._

import scala.concurrent.{ExecutionContext, Future}

sealed trait InstitutionProjectionCommand
case class ProjectEvent(evt: InstitutionEvent)
    extends InstitutionProjectionCommand

object InstitutionDBProjection extends InstitutionEmailComponent {

  val config = ConfigFactory.load()

  val name = "InstitutionDBProjector"

  implicit val institutionRepository2018 = new InstitutionRepository2018(
    dbConfig)
  implicit val institutionRepository2019 = new InstitutionRepository2019(
    dbConfig)
  implicit val institutionRepository2019Beta = new InstitutionRepository2019(dbConfig)
  implicit val institutionEmailsRepository = new InstitutionEmailsRepository(
    dbConfig)

  implicit val ec: ExecutionContext = ExecutionContext.global

  val isBeta = config.getString("hmda.institution.isBeta").toBoolean

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
      case InstitutionDeleted(lei, year) =>
        if (year == 2018) {
          institutionRepository2018.deleteById(lei)
        } else if (year == 2019) {
          if (isBeta) {
            institutionRepository2019Beta.deleteById(lei)
          }
          else {
            institutionRepository2019.deleteById(lei)
          }
        }
    }
    event
  }

  private def updateTables(inst: Institution): Future[List[Int]] = {
    val insertResult = {
      if (inst.activityYear == 2018) {
        institutionRepository2018.insertOrUpdate(
          InstitutionConverter.convert(inst))
      } else {
        if (isBeta) {
          institutionRepository2019Beta.insertOrUpdate(InstitutionConverter.convert(inst))
        }
        else {
          institutionRepository2019.insertOrUpdate(
            InstitutionConverter.convert(inst))
        }
      }
    }
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
