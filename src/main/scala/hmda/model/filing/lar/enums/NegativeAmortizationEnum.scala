package hmda.model.filing.lar.enums

sealed trait NegativeAmortizationEnum extends LarEnum

object NegativeAmortizationEnum extends LarCodeEnum[NegativeAmortizationEnum] {
  override val values = List(1, 2)

  override def valueOf(code: Int): NegativeAmortizationEnum = {
    code match {
      case 1 => NegativeAmortization
      case 2 => NoNegativeAmortization
      case _ => throw new Exception("Invalid Negative Amortization Code")
    }
  }
}

case object NegativeAmortization extends NegativeAmortizationEnum {
  override val code: Int = 1
  override val description: String = "Negative amortization"
}

case object NoNegativeAmortization extends NegativeAmortizationEnum {
  override val code: Int = 2
  override val description: String = "No negative amortization"
}
