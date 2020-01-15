package hmda.model.filing.lar.enums

sealed trait ManufacturedHomeLandPropertyInterestEnum extends LarEnum

object ManufacturedHomeLandPropertyInterestEnum
    extends LarCodeEnum[ManufacturedHomeLandPropertyInterestEnum] {
  override val values = List(1, 2, 3, 4, 5, 1111)

  override def valueOf(code: Int): ManufacturedHomeLandPropertyInterestEnum =
    code match {
      case 1    => DirectOwnership
      case 2    => IndirectOwnership
      case 3    => PaidLeasehold
      case 4    => UnpaidLeasehold
      case 5    => ManufacturedHomeLandNotApplicable
      case 1111 => ManufacturedHomeLoanPropertyInterestExempt
      case other    => new InvalidManufacturedHomeLandPropertyCode(other)
    }
}

case object DirectOwnership extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int           = 1
  override val description: String = "Direct ownership"
}

case object IndirectOwnership extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int           = 2
  override val description: String = "Indirect ownership"
}

case object PaidLeasehold extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int           = 3
  override val description: String = "Paid leasehold"
}

case object UnpaidLeasehold extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int           = 4
  override val description: String = "Unpaid leasehold"
}

case object ManufacturedHomeLandNotApplicable extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int           = 5
  override val description: String = "Not applicable"
}

case object ManufacturedHomeLoanPropertyInterestExempt extends ManufacturedHomeLandPropertyInterestEnum {
  override def code: Int = 1111
  override def description: String =
    "Exempt Manufactured Home Loan Property Interest"
}

class InvalidManufacturedHomeLandPropertyCode(value: Int = -1) extends ManufacturedHomeLandPropertyInterestEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidManufacturedHomeLandPropertyCode => true
            case _ => false
        }
}
