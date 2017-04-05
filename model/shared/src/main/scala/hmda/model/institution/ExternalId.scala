package hmda.model.institution

import enumeratum.{ Enum, EnumEntry }

/**
 * Additional unique identifiers for a financial institution.
 */
case class ExternalId(value: String, externalIdType: ExternalIdType)

sealed abstract class ExternalIdType(override val entryName: String, val formattedName: String) extends EnumEntry with Serializable

object ExternalIdType extends Enum[ExternalIdType] {

  val values = findValues

  case object FdicCertNo extends ExternalIdType("fdic-certificate-number", "FDIC Certificate Number")
  case object FederalTaxId extends ExternalIdType("federal-tax-id", "Federal Tax ID")
  case object NcuaCharterId extends ExternalIdType("ncua-charter-id", "NCUA Charter Number")
  case object OccCharterId extends ExternalIdType("occ-charter-id", "OCC Charter Number")
  case object RssdId extends ExternalIdType("rssd-id", "RSSD ID")
  case object UndeterminedExternalId extends ExternalIdType("undetermined-external-id", "Unknown ID")
}
