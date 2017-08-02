package hmda.model.publication.reports

import enumeratum.values.{ IntEnum, IntEnumEntry }

sealed abstract class EthnicityEnum(
  override val value: Int,
  val description: String
) extends IntEnumEntry

object EthnicityEnum extends IntEnum[EthnicityEnum] {

  val values = findValues

  case object HispanicOrLatino extends EthnicityEnum(1, "Hispanic or Latino")
  case object NotHispanicOrLatino extends EthnicityEnum(2, "Not Hispanic or Latino")
  case object NotAvailable extends EthnicityEnum(3, "Ethnicity not available")
  case object Joint extends EthnicityEnum(0, "Joint (Hispanic or Latino/Not Hispanic or Latino")
}
