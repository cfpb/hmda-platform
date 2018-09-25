package hmda.institution.projection

import akka.persistence.query.EventEnvelope
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.InstitutionConverter
import hmda.institution.query.InstitutionComponent
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionDeleted,
  InstitutionModified
}
import hmda.projection.ResumableProjection
import hmda.query.DbConfiguration._

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

  override def projectEvent(envelope: EventEnvelope): EventEnvelope = {
    val event = envelope.event
    event match {
      case InstitutionCreated(i) =>
        institutionRepository.insertOrUpdate(InstitutionConverter.convert(i))
        val emails = InstitutionConverter.emailsFromInstitution(i)
        emails.foreach(email =>
          institutionEmailsRepository.insertOrUpdate(email))

      case InstitutionModified(i) =>
        institutionRepository.insertOrUpdate(InstitutionConverter.convert(i))
        val emails = InstitutionConverter.emailsFromInstitution(i)
        emails.foreach(email =>
          institutionEmailsRepository.insertOrUpdate(email))
        
      case InstitutionDeleted(lei) =>
        institutionRepository.deleteById(lei)
    }
    envelope
  }

}
