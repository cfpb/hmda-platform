package hmda.model.institution

import enumeratum.{ Enum, EnumEntry }

/**
 * Additional unique identifiers for a financial institution.
 */
case class ExternalId(value: String, name: ExternalIdType)

sealed abstract class ExternalIdType(override val entryName: String) extends EnumEntry with Serializable

object ExternalIdType extends Enum[ExternalIdType] {

  val values = findValues

  case object FdicCertNo extends ExternalIdType("fdic-certificate-number")
  case object FederalTaxId extends ExternalIdType("federal-tax-id")
  case object NcuaCharterId extends ExternalIdType("ncua-charter-id")
  case object OccCharterId extends ExternalIdType("occ-charter-id")
  case object RssdId extends ExternalIdType("rssd-id")
  case object UndeterminedExternalId extends ExternalIdType("undetermined-external-id")
}
