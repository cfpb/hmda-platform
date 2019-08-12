package hmda.dataBrowser.models

import enumeratum._

import scala.collection.immutable

sealed abstract class Race(override val entryName: String) extends EnumEntry

object Race extends Enum[Race] {
  val values: immutable.IndexedSeq[Race] = findValues

  case object Asian extends Race("Asian")
  case object NativeHawaiianOrOtherPacificIslander
      extends Race("Native Hawaiian or Other Pacific Islander")
  case object FreeFormTextOnly extends Race("Free Form Text Only")
  case object RaceNotAvailable extends Race("Race Not Available")
  case object AmericanIndianOrAlaskaNative
      extends Race("American Indian or Alaska Native")
  case object BlackOrAfricanAmerican extends Race("Black or African American")
  case object TwoOrMoreMinorityRaces extends Race("2 or more minority races")
  case object White extends Race("White")
  case object Joint extends Race("Joint")

  def validateRaces(rawRaces: Seq[String]): Either[Seq[String], Seq[Race]] = {
    val potentialRaces =
      rawRaces.map(action => (action, Race.withNameInsensitiveOption(action)))
    val isRacesValid = potentialRaces.map(_._2).forall(_.isDefined)

    if (isRacesValid) Right(potentialRaces.flatMap(_._2))
    else
      Left(
        potentialRaces.collect {
          case (input, None) => input
        }
      )
  }
}
