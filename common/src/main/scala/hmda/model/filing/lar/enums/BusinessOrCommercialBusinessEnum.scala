package hmda.model.filing.lar.enums

sealed trait BusinessOrCommercialBusinessEnum extends LarEnum

object BusinessOrCommercialBusinessEnum
    extends LarCodeEnum[BusinessOrCommercialBusinessEnum] {
  override val values = List(1, 2, 1111)

  override def valueOf(code: Int): BusinessOrCommercialBusinessEnum =
    code match {
      case 1    => PrimarilyBusinessOrCommercialPurpose
      case 2    => NotPrimarilyBusinessOrCommercialPurpose
      case 1111 => ExemptBusinessOrCommercialPurpose
      case other    => new InvalidBusinessOrCommercialBusinessCode(other)
    }
}

case object PrimarilyBusinessOrCommercialPurpose extends BusinessOrCommercialBusinessEnum {
  override val code: Int = 1
  override val description: String =
    "Primarily for a business or commercial purpose"
}

case object NotPrimarilyBusinessOrCommercialPurpose extends BusinessOrCommercialBusinessEnum {
  override val code: Int = 2
  override val description: String =
    "Not primarily for a business or commercial purpose"
}

case object ExemptBusinessOrCommercialPurpose extends BusinessOrCommercialBusinessEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt business or commercial purpose"
}

class InvalidBusinessOrCommercialBusinessCode(value: Int = -1) extends BusinessOrCommercialBusinessEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidBusinessOrCommercialBusinessCode => true
            case _ => false
        }
}
