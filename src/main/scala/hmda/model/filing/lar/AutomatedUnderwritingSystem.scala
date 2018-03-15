package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.AutomatedUnderwritingSystemEnum

case class AutomatedUnderwritingSystem(
    aus1: AutomatedUnderwritingSystemEnum,
    aus2: AutomatedUnderwritingSystemEnum,
    aus3: AutomatedUnderwritingSystemEnum,
    aus4: AutomatedUnderwritingSystemEnum,
    aus5: AutomatedUnderwritingSystemEnum,
    otherAUS: String
) extends PipeDelimited {
  override def toCSV: String = {
    s"${aus1.code}|${aus2.code}|${aus3.code}|${aus4.code}|${aus5.code}|$otherAUS"
  }
}
