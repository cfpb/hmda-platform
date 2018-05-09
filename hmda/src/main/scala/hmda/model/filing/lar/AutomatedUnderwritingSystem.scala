package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.{
  AutomatedUnderwritingSystemEnum,
  InvalidAutomatedUnderwritingSystemCode
}

case class AutomatedUnderwritingSystem(
    aus1: AutomatedUnderwritingSystemEnum =
      InvalidAutomatedUnderwritingSystemCode,
    aus2: AutomatedUnderwritingSystemEnum =
      InvalidAutomatedUnderwritingSystemCode,
    aus3: AutomatedUnderwritingSystemEnum =
      InvalidAutomatedUnderwritingSystemCode,
    aus4: AutomatedUnderwritingSystemEnum =
      InvalidAutomatedUnderwritingSystemCode,
    aus5: AutomatedUnderwritingSystemEnum =
      InvalidAutomatedUnderwritingSystemCode,
    otherAUS: String = ""
) extends PipeDelimited {
  override def toCSV: String = {
    s"${aus1.code}|${aus2.code}|${aus3.code}|${aus4.code}|${aus5.code}|$otherAUS"
  }
}
