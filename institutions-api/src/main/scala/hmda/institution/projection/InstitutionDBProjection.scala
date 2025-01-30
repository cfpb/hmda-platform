package hmda.institution.projection

import java.time.Instant

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionConverter
import hmda.institution.query.{InstitutionNoteHistoryEntity, _}
import hmda.messages.institution.InstitutionEvents.{InstitutionCreated, InstitutionDeleted, InstitutionEvent, InstitutionModified}
import hmda.model.institution.Institution
import hmda.query.DbConfiguration._
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

sealed trait InstitutionProjectionCommand
case class ProjectEvent(evt: InstitutionEvent) extends InstitutionProjectionCommand

object InstitutionDBProjection extends InstitutionEmailComponent with InstitutionNoteHistoryComponent {

  val config = ConfigFactory.load()

  val name = "InstitutionDBProjector"

  /**
    * Note: institutions-api microservice reads the JDBC_URL env var from inst-postgres-credentials secret.
    * In beta namespace this environment variable has currentSchema=hmda_beta_user appended to it to change the schema
    * to BETA
    */

  implicit val institutionRepository2018   = new InstitutionRepository(dbConfig, "institutions2018")
  implicit val institutionRepository2019   = new InstitutionRepository(dbConfig, "institutions2019")
  implicit val institutionRepository2020   = new InstitutionRepository(dbConfig, "institutions2020")
  implicit val institutionRepository2021   = new InstitutionRepository(dbConfig, "institutions2021")
  implicit val institutionRepository2022   = new InstitutionRepository(dbConfig, "institutions2022")
  implicit val institutionRepository2023   = new InstitutionRepository(dbConfig, "institutions2023")
  implicit val institutionRepository2024   = new InstitutionRepository(dbConfig, "institutions2024")
  implicit val institutionRepository2025   = new InstitutionRepository(dbConfig, "institutions2025")



  implicit val institutionEmailsRepository = new InstitutionEmailsRepository(dbConfig)
  implicit val institutionNotesHistoryRepository = new InstitutionNoteHistoryRepository(dbConfig)

  implicit val ec: ExecutionContext = ExecutionContext.global
  val log = LoggerFactory.getLogger("hmda")

  val behavior: Behavior[InstitutionProjectionCommand] =
    Behaviors.setup { ctx =>
      val log = ctx.log

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
          case 2021 =>
            institutionRepository2021.deleteById(lei)
          case 2022 =>
            institutionRepository2022.deleteById(lei)
          case 2023 =>
            institutionRepository2023.deleteById(lei)
          case 2024 =>
            institutionRepository2024.deleteById(lei)
          case 2025 =>
            institutionRepository2025.deleteById(lei)
          case _ =>
            institutionRepository2024.deleteById(lei)
        }
      case other => log.error(s"Unexpected event passed to Institution DB Projector: ${other}")
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
        case 2021 =>
          institutionRepository2021.insertOrUpdate(InstitutionConverter.convert(inst))
        case 2022 =>
          institutionRepository2022.insertOrUpdate(InstitutionConverter.convert(inst))
        case 2023 =>
          institutionRepository2023.insertOrUpdate(InstitutionConverter.convert(inst))
        case 2024 =>
          institutionRepository2024.insertOrUpdate(InstitutionConverter.convert(inst))
        case 2025 =>
          institutionRepository2025.insertOrUpdate(InstitutionConverter.convert(inst))
        case _ =>
          institutionRepository2024.insertOrUpdate(InstitutionConverter.convert(inst))
      }
    }

    val institutionNoteHistoryEntity: InstitutionNoteHistoryEntity = generateHistoryID(inst)
    institutionNotesHistoryRepository.insertOrUpdate(institutionNoteHistoryEntity)

    val emails = InstitutionConverter.emailsFromInstitution(inst).toList
    for {
      institutionRow <- insertResult
      emailsRows     <- updateEmailsInSerial(emails)
    } yield emailsRows :+ institutionRow

  }

  private def updateEmailsInSerial(emails: List[InstitutionEmailEntity]): Future[List[Int]] =
    Future.traverse(emails)(updateEmails)

  private def generateHistoryID(inst: Institution): InstitutionNoteHistoryEntity = {
    val timestamp = Instant.now().toEpochMilli

    val historyID = inst.LEI + "-" + inst.activityYear + "-" + timestamp

    import io.circe.syntax._
       InstitutionNoteHistoryEntity(
       lei = inst.LEI,
       historyID = historyID,
       notes = inst.notes,
       year = inst.activityYear.toString(),
       updatedPanel = inst.asJson.noSpaces
     )
  }
}


