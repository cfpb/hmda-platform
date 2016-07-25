package hmda.model.institution

import enumeratum.{ Enum, EnumEntry }

/**
 * Created by keelerh on 7/22/16.
 */
sealed abstract class InstitutionStatus(override val entryName: String) extends EnumEntry

object InstitutionStatus extends Enum[InstitutionStatus] {

  val values = findValues

  case object Active extends InstitutionStatus("active")
  case object Inactive extends InstitutionStatus("inactive")

}
