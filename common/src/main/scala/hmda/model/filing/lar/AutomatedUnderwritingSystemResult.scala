package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.{ AutomatedUnderwritingResultEnum, InvalidAutomatedUnderwritingResultCode }

case class AutomatedUnderwritingSystemResult(
  ausResult1: AutomatedUnderwritingResultEnum = InvalidAutomatedUnderwritingResultCode,
  ausResult2: AutomatedUnderwritingResultEnum = InvalidAutomatedUnderwritingResultCode,
  ausResult3: AutomatedUnderwritingResultEnum = InvalidAutomatedUnderwritingResultCode,
  ausResult4: AutomatedUnderwritingResultEnum = InvalidAutomatedUnderwritingResultCode,
  ausResult5: AutomatedUnderwritingResultEnum = InvalidAutomatedUnderwritingResultCode,
  otherAusResult: String = ""
) extends PipeDelimited {
  override def toCSV: String =
    s"${ausResult1.code}|${ausResult2.code}|${ausResult3.code}|${ausResult4.code}|${ausResult5.code}|$otherAusResult"
}
