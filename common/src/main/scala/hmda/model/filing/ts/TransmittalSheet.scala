package hmda.model.filing.ts

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.{HmdaFileRow, PipeDelimited}
import hmda.model.institution.{Agency, UndeterminedAgency}
//import hmda.validation.{AS, EC, MAT}

import scala.concurrent.Future

case class TransmittalSheet(
    id: Int = 1,
    institutionName: String = "",
    year: Int = 2018,
    quarter: Int = 4,
    contact: Contact = Contact(),
    agency: Agency = UndeterminedAgency,
    totalLines: Int = 0,
    taxId: String = "",
    LEI: String = ""
//    LARs: List[LoanApplicationRegister] = List.empty
) extends PipeDelimited
    with HmdaFileRow {
  override def toCSV: String = {
    s"$id|$institutionName|$year|$quarter|${contact.toCSV}|${agency.code}|$totalLines|$taxId|$LEI"
  }

  override def valueOf(field: String): String = {
    TsFieldMapping
      .mapping(this)
      .getOrElse(field, s"error: field name mismatch for $field")
  }

}
