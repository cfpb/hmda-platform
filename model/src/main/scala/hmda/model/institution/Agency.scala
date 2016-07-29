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
  val fullName: String,
  val externalIdTypes: Map[DepositoryType, ExternalIdType]
) extends IntEnumEntry

object Agency extends IntEnum[Agency] {

  val values = findValues

  case object CFPB extends Agency(9, "cfpb", "Consumer Financial Protection Bureau", Map(Depository -> RssdId, NonDepository -> FederalTaxId))
  case object FDIC extends Agency(3, "fdic", "Federal Deposit Insurance Corporation", Map(Depository -> FdicCertNo, NonDepository -> FederalTaxId))
  case object FRS extends Agency(2, "frs", "Federal Reserve System", Map(Depository -> RssdId, NonDepository -> RssdId))
  case object HUD extends Agency(7, "hud", "Housing and Urban Development", Map(NonDepository -> FederalTaxId))
  case object NCUA extends Agency(5, "ncua", "National Credit Union Administration", Map(Depository -> NcuaCharterId, NonDepository -> FederalTaxId))
  case object OCC extends Agency(1, "occ", "Office of the Comptroller of the Currency", Map(Depository -> OccCharterId, NonDepository -> FederalTaxId))
}