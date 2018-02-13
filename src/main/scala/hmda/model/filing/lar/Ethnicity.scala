package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.EthnicityEnum

case class Ethnicity(
    ethnicity1: EthnicityEnum,
    ethnicity2: EthnicityEnum,
    ethnicity3: EthnicityEnum,
    ethnicity4: EthnicityEnum,
    ethnicity5: EthnicityEnum
) extends PipeDelimited {
  override def toCSV: String = {
    s"${ethnicity1.code}|${ethnicity2.code}|${ethnicity3.code}|${ethnicity4.code}|${ethnicity5.code}"
  }
}
