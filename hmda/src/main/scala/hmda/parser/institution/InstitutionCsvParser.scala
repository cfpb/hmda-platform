package hmda.parser.institution

import hmda.model.institution._

object InstitutionCsvParser {

  def apply(s: String): Institution = {
    val values = s.split('|').map(_.trim).toList
    val acticityYear = values.head.toInt
    val lei = values(1)
    val agencyCode = values(2).toInt
    val institutionTypeCode = values(3).toInt
    val instId2017 = values(4)
    val taxId = values(5)
    val rssd = values(6).toInt
    val emailDomains = values(7)
    val respondentName = values(8)
    val respondentState = values(9)
    val respondentCity = values(10)
    val parentIdRssd = values(11).toInt
    val parentName = values(12)
    val assets = values(13).toInt
    val otherLenderCode = values(14).toInt
    val topHolderIdRssd = values(15).toInt
    val topHolderName = values(16)
    val hmdaFiler = values(17)

    Institution(
      acticityYear,
      lei,
      Agency.valueOf(agencyCode.toInt),
      InstitutionType.valueOf(institutionTypeCode),
      if (instId2017 == "") None else Some(instId2017),
      if (taxId == "") None else Some(taxId),
      rssd.toInt,
      emailDomains.split(',').toList,
      Respondent(
        if (respondentName == "") None else Some(respondentName),
        if (respondentState == "") None else Some(respondentState),
        if (respondentCity == "") None else Some(respondentCity)
      ),
      Parent(
        parentIdRssd,
        if (parentName == "") None else Some(parentName)
      ),
      assets,
      otherLenderCode,
      TopHolder(
        topHolderIdRssd,
        if (topHolderName == "") None else Some(topHolderName)
      ),
      if (hmdaFiler == "") false else hmdaFiler.toBoolean
    )

  }

}
