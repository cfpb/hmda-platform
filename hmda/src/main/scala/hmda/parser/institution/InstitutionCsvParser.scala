package hmda.parser.institution

import hmda.model.institution._

object InstitutionCsvParser {

  def apply(s: String): Institution = {
    val values = s.split('|').map(_.trim).toList
    val acticityYear = values.head.toInt
    val lei = values(1)
    val agencyCode = values(2)
    val institutionTypeCode = values(3)
    val instId2017 = values(4)
    val taxId = values(5)
    val rssd = values(6)
    val emailDomains = values(7)
    val respondentName = values(8)
    val respondentState = values(9)
    val respondentCity = values(10)
    val parentIdRssd = values(11)
    val parentName = values(12)
    val assets = values(13)
    val otherLenderCode = values(14)
    val topHolderIdRssd = values(15)
    val topHolderName = values(16)
    val hmdaFiler = values(17)

    Institution(
      acticityYear,
      if (lei == "") None else Some(lei),
      if (agencyCode == "") Some(UndeterminedAgency)
      else Some(Agency.valueOf(agencyCode.toInt)),
      if (institutionTypeCode == "") Some(UndeterminedInstitutionType)
      else Some(InstitutionType.valueOf(institutionTypeCode.toInt)),
      if (instId2017 == "") None else Some(instId2017),
      if (taxId == "") None else Some(taxId),
      if (rssd == "") None else Some(rssd),
      emailDomains = emailDomains.split(','),
      Respondent(
        if (respondentName == "") None else Some(respondentName),
        if (respondentState == "") None else Some(respondentState),
        if (respondentCity == "") None else Some(respondentCity)
      ),
      Parent(
        if (parentIdRssd == "" || parentIdRssd == "0") None
        else Some(parentIdRssd.toInt),
        if (parentName == "") None else Some(parentName)
      ),
      if (assets == "" || assets == "0") None else Some(assets.toInt),
      if (otherLenderCode == "" || otherLenderCode == "0") None
      else Some(otherLenderCode.toInt),
      TopHolder(
        if (topHolderIdRssd == "" || topHolderIdRssd == "0") None
        else Some(topHolderIdRssd.toInt),
        if (topHolderName == "") None else Some(topHolderName)
      ),
      if (hmdaFiler == "") false else hmdaFiler.toBoolean
    )

  }

}
