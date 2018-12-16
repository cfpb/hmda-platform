package hmda.institution.projection

import akka.persistence.query.EventEnvelope
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionConverter
import hmda.institution.query.{InstitutionComponent, InstitutionEmailEntity}
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionModified
}
import hmda.model.institution.Institution
import hmda.projection.ResumableProjection
import hmda.query.DbConfiguration._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object InstitutionDBProjection
    extends ResumableProjection
    with InstitutionComponent {

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.institution.timeout")

  override val name = "InstitutionDBProjector"

  override implicit val timeout = Timeout(duration.seconds)

  implicit val institutionRepository = new InstitutionRepository(dbConfig)
  implicit val institutionEmailsRepository = new InstitutionEmailsRepository(
    dbConfig)

  implicit val ec: ExecutionContext = ExecutionContext.global

  override def projectEvent(envelope: EventEnvelope): EventEnvelope = {
    val event = envelope.event
    event match {
      case InstitutionCreated(i) =>
        updateTables(i)

      case InstitutionModified(i) =>
        updateTables(i)

      case InstitutionDeleted(lei) =>
        institutionRepository.deleteById(lei)
        deleteEmails(lei)
    }
    envelope
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
