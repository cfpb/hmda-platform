package hmda.model.filing.lar.enums

sealed trait BusinessOrCommercialBusinessEnum {
  val code: Int
  val description: String
}

object BusinessOrCommercialBusinessEnum {
  val values = List(1, 2)

  def valueOf(code: Int): BusinessOrCommercialBusinessEnum = {
    code match {
      case 1 => PrimarilyBusinessOrCommercialPurpose
      case 2 => NotPrimarilyBusinessOrCommercialPurpose
      case _ => throw new Exception("Invalid Business Or Commercial Type Code")
    }
  }
}

case object PrimarilyBusinessOrCommercialPurpose
    extends BusinessOrCommercialBusinessEnum {
  override val code: Int = 1
  override val description: String =
    "Primarily for a business or commercial purpose"
}

case object NotPrimarilyBusinessOrCommercialPurpose
    extends BusinessOrCommercialBusinessEnum {
  override val code: Int = 2
  override val description: String =
    "Not primarily for a business or commercial purpose"
}
