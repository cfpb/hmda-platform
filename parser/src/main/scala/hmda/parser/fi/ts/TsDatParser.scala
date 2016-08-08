package hmda.parser.fi.ts

import hmda.model.fi.ts.{ Contact, Parent, Respondent, TransmittalSheet }

object TsDatParser {
  def apply(s: String): TransmittalSheet = {
    val id = s.substring(0, 1).toInt
    val respId = s.substring(1, 11).trim.replaceFirst("^0+(?!$)", "")
    val code = s.substring(11, 12).trim.toInt
    val timestamp = s.substring(12, 24).trim.toLong
    val activityYear = s.substring(25, 29).trim.toInt
    val taxId = s.substring(29, 39).trim
    val totalLines = s.substring(39, 46).trim.toInt
    val respName = s.substring(46, 76).trim
    val respAddress = s.substring(76, 116).trim
    val respCity = s.substring(116, 141).trim
    val respState = s.substring(141, 143)
    val respZip = s.substring(143, 153).trim
    val parentName = s.substring(153, 183).trim
    val parentAddress = s.substring(183, 223).trim
    val parentCity = s.substring(223, 248).trim
    val parentState = s.substring(248, 250).trim
    val parentZip = s.substring(250, 260).trim
    val contactPerson = s.substring(260, 290).trim
    val contactPhone = s.substring(290, 302).trim
    val contactFax = s.substring(302, 314).trim
    val contactEmail = s.substring(314, s.length).trim

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
