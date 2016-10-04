package hmda.persistence.processing.serialization

import hmda.fi.ts.TransmittalSheetMessage
import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import scala.language.implicitConversions

object TsConverter {

  implicit def messageToTransmittalSheet(m: TransmittalSheetMessage): TransmittalSheet = {
    val id = m.id
    val respondentId = m.respondentId
    val agencyCode = m.agencyCode
    val timestamp = m.timestamp
    val activityYear = m.activityYear
    val taxId = m.taxId
    val totalLines = m.totalLines
    val respondentName = m.respondentName
    val respondentAddress = m.respondentAddress
    val respondentCity = m.respondentCity
    val respondentState = m.respondentState
    val respondentZipCode = m.respondentZipCode
    val parentName = m.respondentName
    val parentAddress = m.parentAddress
    val parentCity = m.parentCity
    val parentState = m.parentState
    val parentZipCode = m.parentZipCode
    val contactName = m.contactName
    val contactPhone = m.contactPhone
    val contactFax = m.contactFax
    val contactEmail = m.contactEmail

    val respondent = Respondent(
      respondentId,
      respondentName,
      respondentAddress,
      respondentCity,
      respondentState,
      respondentZipCode
    )

    val parent = Parent(
      parentName,
      parentAddress,
      parentCity,
      parentState,
      parentZipCode
    )

    val contact = Contact(
      contactName,
      contactPhone,
      contactFax,
      contactEmail
    )

    TransmittalSheet(
      id,
      agencyCode,
      timestamp,
      activityYear,
      taxId,
      totalLines,
      respondent,
      parent,
      contact
    )
  }

  implicit def transmittalSheetToMessage(ts: TransmittalSheet): Option[TransmittalSheetMessage] = {
    val id = ts.id
    val respondentId = ts.respondent.id
    val agencyCode = ts.agencyCode
    val timestamp = ts.timestamp
    val activityYear = ts.activityYear
    val taxId = ts.taxId
    val totalLines = ts.totalLines
    val respondentName = ts.respondent.name
    val respondentAddress = ts.respondent.address
    val respondentCity = ts.respondent.city
    val respondentState = ts.respondent.state
    val respondentZipCode = ts.respondent.zipCode
    val parentName = ts.parent.name
    val parentAddress = ts.parent.address
    val parentCity = ts.parent.city
    val parentState = ts.parent.state
    val parentZipCode = ts.parent.zipCode
    val contactName = ts.contact.name
    val contactPhone = ts.contact.phone
    val contactFax = ts.contact.fax
    val contactEmail = ts.contact.email

    Some(
      TransmittalSheetMessage(
        id,
        respondentId,
        agencyCode,
        timestamp,
        activityYear,
        taxId,
        totalLines,
        respondentName,
        respondentAddress,
        respondentCity,
        respondentState,
        respondentZipCode,
        parentName,
        parentAddress,
        parentCity,
        parentState,
        parentZipCode,
        contactName,
        contactPhone,
        contactFax,
        contactEmail
      )
    )

  }
}
