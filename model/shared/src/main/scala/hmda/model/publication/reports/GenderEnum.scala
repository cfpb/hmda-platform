package hmda.model.publication.reports

import enumeratum.values.{ IntEnum, IntEnumEntry }

sealed abstract class GenderEnum(
  override val value: Int,
  val description: String
) extends IntEnumEntry

object GenderEnum extends IntEnum[GenderEnum] {

  val values = findValues

  case object Male extends GenderEnum(1, "Male")
  case object Female extends GenderEnum(2, "Female")
  case object JointGender extends GenderEnum(3, "Joint (Male/Female)")
  case object GenderNotAvailable extends GenderEnum(4, "Gender Not Available")
}
