package hmda.parser.institution

import hmda.model.institution._
import hmda.util.CSVConsolidator.{listDeDupeToList, stringDeDupeToList}
import io.chrisdavenport.cormorant.parser.CSVLikeParser
import io.chrisdavenport.cormorant

object InstitutionCsvParser {
  val parser: CSVLikeParser = new CSVLikeParser('|') {}

  def apply(s: String): Institution = {
    val values              = cormorant.parser.parseRow(s, parser).toTry.get.l.map(_.x).toList
    val activityYear        = values.head.toInt
    val lei                 = values(1)
    val agencyCode          = values(2).toInt
    val institutionTypeCode = values(3).toInt
    val instId2017          = values(4)
    val taxId               = values(5)
    val rssd                = values(6).toInt
    val emailDomains        = values(7)
    val respondentName      = values(8)
    val respondentState     = values(9)
    val respondentCity      = values(10)
    val parentIdRssd        = values(11).toInt
    val parentName          = values(12)
    val assets              = values(13).toLong
    val otherLenderCode     = values(14).toInt
    val topHolderIdRssd     = values(15).toInt
    val topHolderName       = values(16)
    val hmdaFiler           = values(17)
    val quarterlyFiler      = values(18)
    val quarterlyFilerHasFiledQ1 = values(19)
    val quarterlyFilerHasFiledQ2 = values(20)
    val quarterlyFilerHasFiledQ3 = values(21)
    val notes                    = values.lift.apply(22).getOrElse("") //TODO consider default value from env

    val emails =
      if (emailDomains.isEmpty) List() else stringDeDupeToList(emailDomains)

    Institution(
      activityYear,
      lei,
      Agency.valueOf(agencyCode.toInt),
      InstitutionType.valueOf(institutionTypeCode),
      if (instId2017 == "") None else Some(instId2017),
      if (taxId == "") None else Some(taxId),
      rssd.toInt,
      emails,
      Respondent(
        if (respondentName == "") None else Some(respondentName),
        if (respondentState == "") None else Some(respondentState),
        if (respondentCity == "") None else Some(respondentCity)
      ),
      Parent(parentIdRssd, if (parentName == "") None else Some(parentName)),
      assets,
      otherLenderCode,
      TopHolder(
        topHolderIdRssd,
        if (topHolderName == "") None else Some(topHolderName)
      ),
      parseBoolean(hmdaFiler),
      parseBoolean(quarterlyFiler),
      parseBoolean(quarterlyFilerHasFiledQ1),
      parseBoolean(quarterlyFilerHasFiledQ2),
      parseBoolean(quarterlyFilerHasFiledQ3),
      notes
    )
  }
  def parseBoolean(i: String): Boolean =
    i.toLowerCase() match {
      case "t"     => true
      case "f"     => false
      case "true"  => true
      case "false" => false
      case _       => false
    }
}