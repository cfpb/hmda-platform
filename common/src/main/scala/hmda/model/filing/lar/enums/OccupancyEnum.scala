package hmda.model.filing.lar.enums

sealed trait OccupancyEnum extends LarEnum

object OccupancyEnum extends LarCodeEnum[OccupancyEnum] {
  override val values = List(1, 2, 3)

  override def valueOf(code: Int): OccupancyEnum =
    code match {
      case 1 => PrincipalResidence
      case 2 => SecondResidence
      case 3 => InvestmentProperty
      case other => new InvalidOccupancyCode(other)
    }
}

case object PrincipalResidence extends OccupancyEnum {
  override val code: Int           = 1
  override val description: String = "Principal Residence"
}

case object SecondResidence extends OccupancyEnum {
  override val code: Int           = 2
  override val description: String = "Second Residence"
}

case object InvestmentProperty extends OccupancyEnum {
  override val code: Int           = 3
  override val description: String = "Investment Property"
}

case object InvalidOccupancyExemptCode extends OccupancyEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidOccupancyCode(value: Int = -1) extends OccupancyEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidOccupancyCode => true
            case _ => false
        }
}
