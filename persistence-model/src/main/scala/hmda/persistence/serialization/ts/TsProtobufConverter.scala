package hmda.persistence.serialization.ts

import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }
import hmda.persistence.model.serialization.TransmittalSheet.{ ContactMessage, TransmittalSheetMessage, TsParentMessage, TsRespondentMessage }

object TsProtobufConverter {

  def tsToProtobuf(ts: TransmittalSheet): TransmittalSheetMessage = {
    TransmittalSheetMessage(
      id = ts.id,
      agencyCode = ts.agencyCode,
      timestamp = ts.timestamp,
      activityYear = ts.activityYear,
      taxId = ts.taxId,
      totalLines = ts.totalLines,
      respondent = Some(tsRespondentToProtobuf(ts.respondent)),
      parent = Some(tsParentToProtobuf(ts.parent)),
      contact = Some(contactToProtobuf(ts.contact))
    )
  }
  def tsFromProtobuf(msg: TransmittalSheetMessage): TransmittalSheet = {
    TransmittalSheet(
      id = msg.id,
      agencyCode = msg.agencyCode,
      timestamp = msg.timestamp,
      activityYear = msg.activityYear,
      taxId = msg.taxId,
      totalLines = msg.totalLines,
      respondent = tsRespondentFromProtobuf(msg.respondent.getOrElse(TsRespondentMessage())),
      parent = tsParentFromProtobuf(msg.parent.getOrElse(TsParentMessage())),
      contact = contactFromProtobuf(msg.contact.getOrElse(ContactMessage()))
    )
  }

  def tsRespondentToProtobuf(respondent: Respondent): TsRespondentMessage = {
    TsRespondentMessage(
      id = respondent.id,
      name = respondent.name,
      address = respondent.address,
      city = respondent.city,
      state = respondent.state,
      zipCode = respondent.zipCode
    )
  }
  def tsRespondentFromProtobuf(msg: TsRespondentMessage): Respondent = {
    Respondent(
      id = msg.id,
      name = msg.name,
      address = msg.address,
      city = msg.city,
      state = msg.state,
      zipCode = msg.zipCode
    )
  }

  def tsParentToProtobuf(parent: Parent): TsParentMessage = {
    TsParentMessage(
      name = parent.name,
      address = parent.address,
      city = parent.city,
      state = parent.state,
      zipCode = parent.zipCode
    )
  }
  def tsParentFromProtobuf(msg: TsParentMessage): Parent = {
    Parent(
      name = msg.name,
      address = msg.address,
      city = msg.city,
      state = msg.state,
      zipCode = msg.zipCode
    )
  }

  def contactToProtobuf(contact: Contact): ContactMessage = {
    ContactMessage(
      name = contact.name,
      phone = contact.phone,
      fax = contact.fax,
      email = contact.email
    )
  }
  def contactFromProtobuf(msg: ContactMessage): Contact = {
    Contact(
      name = msg.name,
      phone = msg.phone,
      fax = msg.fax,
      email = msg.email
    )
  }

}
