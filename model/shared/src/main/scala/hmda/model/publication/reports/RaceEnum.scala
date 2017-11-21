package hmda.model.publication.reports

import enumeratum.values.{ IntEnum, IntEnumEntry }

sealed abstract class RaceEnum(
  override val value: Int,
  val description: String
) extends IntEnumEntry

object RaceEnum extends IntEnum[RaceEnum] {

  val values = findValues

  case object AmericanIndianOrAlaskaNative extends RaceEnum(1, "American Indian/Alaska Native")
  case object Asian extends RaceEnum(2, "Asian")
  case object BlackOrAfricanAmerican extends RaceEnum(3, "Black or African American")
  case object HawaiianOrPacific extends RaceEnum(4, "Native Hawaiian or Other Pacific Islander")
  case object White extends RaceEnum(5, "White")
  case object NotProvided extends RaceEnum(6, "Race Not Available")
  case object TwoOrMoreMinority extends RaceEnum(7, "2 or more minority races")
  case object JointRace extends RaceEnum(8, "Joint (White/Minority Race)")
}
