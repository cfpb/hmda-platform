package hmda.model.institution

import enumeratum.{ Enum, EnumEntry }

/**
 * The status of a financial institution
 */
sealed abstract class InstitutionStatus(override val entryName: String) extends EnumEntry with Serializable

object InstitutionStatus extends Enum[InstitutionStatus] {

  val values = findValues

  case object Active extends InstitutionStatus("active")
  case object Inactive extends InstitutionStatus("inactive")

}
