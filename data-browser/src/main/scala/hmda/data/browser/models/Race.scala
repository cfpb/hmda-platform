package hmda.data.browser.models

 import enumeratum._

 import scala.collection.immutable

 sealed abstract class Race(override val entryName: String) extends EnumEntry

 object Race extends Enum[Race] {
  val values: immutable.IndexedSeq[Race] = findValues

  case object Asian extends Race("Asian")
  case object NativeHawaiianOrOtherPacificIslander extends Race("Native Hawaiian or Other Pacific Islander")
  case object FreeFormTextOnly extends Race("Free Form Text Only")
  case object RaceNotAvailable extends Race("Race Not Available")
  case object AmericanIndianOrAlaskaNative extends Race("American Indian or Alaska Native")
  case object BlackOrAfricanAmerican extends Race("Black or African American")
  case object TwoOrMoreMinorityRaces extends Race("2 Or More Minority Races")
  case object White extends Race("White")
  case object Joint extends Race("Joint")
}