package hmda.institution.projection

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.{ ActorMaterializer, ActorMaterializerSettings, Supervision }
import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionConverter
import hmda.institution.query._
import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionDeleted, InstitutionEvent, InstitutionModified }
import hmda.model.institution.Institution
import hmda.query.DbConfiguration._

import scala.concurrent.{ ExecutionContext, Future }

sealed trait InstitutionProjectionCommand
case class ProjectEvent(evt: InstitutionEvent) extends InstitutionProjectionCommand

object InstitutionDBProjection extends InstitutionEmailComponent {

  val config = ConfigFactory.load()

  val name = "InstitutionDBProjector"

  /**
    * Note: institutions-api microservice reads the JDBC_URL env var from inst-postgres-credentials secret.
    * In beta namespace this environment variable has currentSchema=hmda_beta_user appended to it to change the schema
    * to BETA
    */
  implicit val institutionRepository2018   = new InstitutionRepository2018(dbConfig, "institutions2018")
  implicit val institutionRepository2019   = new InstitutionRepository2019(dbConfig, "institutions2019")
  implicit val institutionRepository2020   = new InstitutionRepository2020(dbConfig, "institutions2020")
  implicit val institutionEmailsRepository = new InstitutionEmailsRepository(dbConfig)

  implicit val ec: ExecutionContext = ExecutionContext.global

  val behavior: Behavior[InstitutionProjectionCommand] =
    Behaviors.setup { ctx =>
      val log = ctx.log
      val decider: Supervision.Decider = {
        case e: Throwable =>
          log.error(e.getLocalizedMessage)
          Supervision.Resume
      }
      implicit val system       = ctx.system.toUntyped
      implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

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
        year match {
          case 2018 =>
            institutionRepository2018.deleteById(lei)
          case 2019 =>
            institutionRepository2019.deleteById(lei)
          case 2020 =>
            institutionRepository2020.deleteById(lei)
        }
    }
    event
  }

  private def updateTables(inst: Institution): Future[List[Int]] = {
    val insertResult = {
      inst.activityYear match {
        case 2018 =>
          institutionRepository2018.insertOrUpdate(InstitutionConverter.convert(inst))
        case 2019 =>
          institutionRepository2019.insertOrUpdate(InstitutionConverter.convert(inst))
        case 2020 =>
          institutionRepository2020.insertOrUpdate(InstitutionConverter.convert(inst))
      }
    }
    val emails = InstitutionConverter.emailsFromInstitution(inst).toList
    for {
      institutionRow <- insertResult
      emailsRows     <- updateEmailsInSerial(emails)
    } yield emailsRows :+ institutionRow
  }

  private def updateEmailsInSerial(emails: List[InstitutionEmailEntity]): Future[List[Int]] =
    emails.foldLeft(Future(List.empty[Int])) { (previousInserts, nextEmail) =>
      for {
        completedInserts <- previousInserts
        insertResult     <- updateEmails(nextEmail)
      } yield completedInserts :+ insertResult
    }

}
