package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.enums.RaceEnum

case class Race(
    race1: RaceEnum,
    race2: RaceEnum,
    race3: RaceEnum,
    race4: RaceEnum,
    race5: RaceEnum
) extends PipeDelimited {
  override def toCSV: String = {
    s"${race1.code}|${race2.code}|${race3.code}|${race4.code}|${race5.code}"
  }
}
