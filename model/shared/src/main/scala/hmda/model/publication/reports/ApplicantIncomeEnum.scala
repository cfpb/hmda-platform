package hmda.model.publication.reports

import enumeratum.values.{ IntEnum, IntEnumEntry }

sealed abstract class ApplicantIncomeEnum(
  override val value: Int,
  val description: String
) extends IntEnumEntry

object ApplicantIncomeEnum extends IntEnum[ApplicantIncomeEnum] {

  val values = findValues

  case object LessThan50PercentOfMSAMedian extends ApplicantIncomeEnum(1, "Less than 50% of MSA/MD median")
  case object Between50And79PercentOfMSAMedian extends ApplicantIncomeEnum(2, "50-79% of MSA/MD median")
  case object Between80And99PercentOfMSAMedian extends ApplicantIncomeEnum(3, "80-99% of MSA/MD median")
  case object Between100And119PercentOfMSAMedian extends ApplicantIncomeEnum(4, "100-119% of MSA/MD median")
  case object GreaterThan120PercentOfMSAMedian extends ApplicantIncomeEnum(5, "120% or more of MSA/MD median")
}
