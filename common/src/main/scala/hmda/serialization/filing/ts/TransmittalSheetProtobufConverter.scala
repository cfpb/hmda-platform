package hmda.serialization.filing.ts

import hmda.model.filing.ts.{ Address, Contact, TransmittalSheet }
import hmda.model.institution.Agency
import hmda.persistence.serialization.transmittalsheet.{ AddressMessage, AgencyMessage, ContactMessage, TransmittalSheetMessage }

object TransmittalSheetProtobufConverter {

  def contactToProtobuf(contact: Contact): ContactMessage = {
    val address = addressToProtobuf(contact.address)
    ContactMessage(contact.name, contact.phone, contact.email, Some(address))
  }

  def contactFromProtobuf(msg: ContactMessage): Contact = {
    val address = addressFromProtobuf(msg.address.getOrElse(AddressMessage()))
    Contact(msg.name, msg.phone, msg.email, address)
  }

  def agencyToProtobuf(agency: Agency): AgencyMessage =
    AgencyMessage(agency.code, agency.name, agency.fullName)

  def agencyFromProtobuf(msg: AgencyMessage): Agency =
    Agency.valueOf(msg.code)

  def addressToProtobuf(address: Address): AddressMessage =
    AddressMessage(address.street, address.city, address.state, address.zipCode)

  def addressFromProtobuf(msg: AddressMessage): Address =
    Address(msg.street, msg.city, msg.state, msg.zipCode)

  def transmittalSheetToProtobuf(ts: TransmittalSheet): TransmittalSheetMessage =
    TransmittalSheetMessage(
      ts.id,
      ts.institutionName,
      ts.year,
      ts.quarter,
      Some(contactToProtobuf(ts.contact)),
      Some(agencyToProtobuf(ts.agency)),
      ts.totalLines,
      ts.taxId,
      ts.LEI
    )

  def transmittalSheetFromProtobuf(msg: TransmittalSheetMessage): TransmittalSheet =
    TransmittalSheet(
      msg.id,
      msg.institutionName,
      msg.year,
      msg.quarter,
      contactFromProtobuf(msg.contact.getOrElse(ContactMessage())),
      agencyFromProtobuf(msg.agency.getOrElse(AgencyMessage())),
      msg.totalLines,
      msg.taxId,
      msg.lEI
    )
}
