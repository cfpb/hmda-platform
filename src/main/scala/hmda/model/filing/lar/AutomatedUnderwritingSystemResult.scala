package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.AutomatedUnderwritingResultEnum

case class AutomatedUnderwritingSystemResult(
    ausResult1: AutomatedUnderwritingResultEnum,
    ausResult2: AutomatedUnderwritingResultEnum,
    ausResult3: AutomatedUnderwritingResultEnum,
    ausResult4: AutomatedUnderwritingResultEnum,
    ausResult5: AutomatedUnderwritingResultEnum,
    otherAusResult: String
) extends PipeDelimited {
  override def toCSV: String = {
    s"$ausResult1|$ausResult2|$ausResult3|$ausResult4|$ausResult5|$otherAusResult"
  }
}
