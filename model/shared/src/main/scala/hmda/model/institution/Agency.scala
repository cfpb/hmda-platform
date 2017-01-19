package hmda.model.institution

import enumeratum.values.{ IntEnum, IntEnumEntry }
import DepositoryType._
import ExternalIdType._
import enumeratum.EnumEntry

/**
 * An institution's regulating federal agency.
 */
sealed abstract class Agency(
  override val value: Int,
  val name: String,
  val fullName: String
) extends IntEnumEntry with Serializable

object Agency extends IntEnum[Agency] {

  val values = findValues

  case object CFPB extends Agency(9, "cfpb", "Consumer Financial Protection Bureau")
  case object FDIC extends Agency(3, "fdic", "Federal Deposit Insurance Corporation")
  case object FRS extends Agency(2, "frs", "Federal Reserve System")
  case object HUD extends Agency(7, "hud", "Housing and Urban Development")
  case object NCUA extends Agency(5, "ncua", "National Credit Union Administration")
  case object OCC extends Agency(1, "occ", "Office of the Comptroller of the Currency")

  case object UndeterminedAgency extends Agency(-1, "undetermined", "Undetermined Agency")
}
