package hmda.model.filing.lar.enums

sealed trait ManufacturedHomeLandPropertyInterestEnum {
  val code: Int
  val description: String
}

object ManufacturedHomeLandPropertyInterestEnum {
  val values = List(1, 2, 3, 4, 5)
//
  def valueOf(code: Int): ManufacturedHomeLandPropertyInterestEnum = {
    code match {
      case 1 => DirectOwnership
      case 2 => IndirectOwnership
      case 3 => PaidLeasehold
      case 4 => UnpaidLeasehold
      case 5 => ManufacturedHomeLandNotApplicable
      case _ =>
        throw new Exception(
          "Invalid Manufactured Home Land Property Interest Code")
    }
  }
}

case object DirectOwnership extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int = 1
  override val description: String = "Direct ownership"
}

case object IndirectOwnership extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int = 2
  override val description: String = "Indirect ownership"
}

case object PaidLeasehold extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int = 3
  override val description: String = "Paid leasehold"
}

case object UnpaidLeasehold extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int = 4
  override val description: String = "Unpaid leasehold"
}

case object ManufacturedHomeLandNotApplicable
    extends ManufacturedHomeLandPropertyInterestEnum {
  override val code: Int = 5
  override val description: String = "Not applicable"
}
