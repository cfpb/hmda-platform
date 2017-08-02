package hmda.model.publication.reports

import enumeratum.values.{ IntEnum, IntEnumEntry }

sealed abstract class MinorityStatusEnum(
  override val value: Int,
  val description: String
) extends IntEnumEntry

object MinorityStatusEnum extends IntEnum[MinorityStatusEnum] {

  val values = findValues

  case object WhiteNonHispanic extends MinorityStatusEnum(1, "White Non-Hispanic")
  case object OtherIncludingHispanic extends MinorityStatusEnum(2, "Others, Including Hispanic")

}
