package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.AutomatedUnderwritingResultEnum

case class AutomatedUnderwritingSystemResult(
    ausResult1: AutomatedUnderwritingResultEnum,
    ausResult2: AutomatedUnderwritingResultEnum,
    ausResult3: AutomatedUnderwritingResultEnum,
    ausResult4: AutomatedUnderwritingResultEnum,
    ausResult5: AutomatedUnderwritingResultEnum
) extends PipeDelimited {
  override def toCSV: String = {
    s"${ausResult1.code}|${ausResult2.code}|${ausResult3.code}|${ausResult4.code}|${ausResult5.code}"
  }
}
