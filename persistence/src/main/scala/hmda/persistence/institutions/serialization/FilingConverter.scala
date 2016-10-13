package hmda.persistence.institutions.serialization

import hmda.model.fi._
import hmda.model.filing.{ FilingMessage, FilingStatusMessage }

import scala.language.implicitConversions

object FilingConverter {
  implicit def messageToFilingCreated(m: Option[FilingMessage]): Filing = {
    m.map { f =>
      val id = f.institutionId
      val period = f.period
      val filingStatus = f.status.value match {
        case 0 => NotStarted
        case 1 => InProgress
        case 2 => Completed
        case 3 => Cancelled
      }
      Filing(period, id, filingStatus)
    }.getOrElse(Filing())
  }

  implicit def filingToMessage(filing: Filing): Option[FilingMessage] = {
    val period = filing.period
    val institutionId = filing.institutionId
    val status = filing.status match {
      case NotStarted => FilingStatusMessage.NOT_STARTED
      case InProgress => FilingStatusMessage.IN_PROGRESS
      case Completed => FilingStatusMessage.COMPLETED
      case Cancelled => FilingStatusMessage.CANCELLED
    }
    val message = FilingMessage(period, institutionId, status)
    Some(message)
  }
}
