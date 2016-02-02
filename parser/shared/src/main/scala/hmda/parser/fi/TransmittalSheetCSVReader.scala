package hmda.parser.fi

import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }

object TransmittalSheetCSVReader {
  def apply(s: String): TransmittalSheet = {
    val values = s.split('|').map(_.trim)
    val id = values(0).toInt
    val respId = values(1)
    val code = values(2).toInt
    val timestamp = values(3).toLong
    val activityYear = values(4).toInt
    val taxId = values(5)
    val totalLines = values(6).toInt
    val respName = values(7)
    val respAddress = values(8)
    val respCity = values(9)
    val respState = values(10)
    val respZip = values(11)
    val parentName = values(12)
    val parentAddress = values(13)
    val parentCity = values(14)
    val parentState = values(15)
    val parentZip = values(16)
    val contactPerson = values(17)
    val contactPhone = values(18)
    val contactFax = values(19)
    val contactEmail = values(20)

    val respondent = Respondent(respId, respName, respAddress, respCity, respState, respZip)
    val parent = Parent(parentName, parentAddress, parentCity, parentState, parentZip)
    val contact = Contact(contactPerson, contactPhone, contactFax, contactEmail)

    TransmittalSheet(
      id,
      code,
      timestamp,
      activityYear,
      taxId,
      totalLines,
      respondent,
      parent,
      contact
    )
  }

}
