package hmda.model.publication.reports

import enumeratum.{ Enum, EnumEntry }

sealed trait CharacteristicEnum extends EnumEntry

object CharacteristicEnum extends Enum[CharacteristicEnum] {

  val values = findValues

  case object Race extends CharacteristicEnum
  case object Ethnicity extends CharacteristicEnum
  case object MinorityStatus extends CharacteristicEnum

}
