package hmda.model.filing.lar.enums

sealed trait NegativeAmortizationEnum extends LarEnum

object NegativeAmortizationEnum extends LarCodeEnum[NegativeAmortizationEnum] {
  override val values = List(1, 2, 1111)

  override def valueOf(code: Int): NegativeAmortizationEnum =
    code match {
      case 1    => NegativeAmortization
      case 2    => NoNegativeAmortization
      case 1111 => NegativeAmortizationExempt
      case _    => InvalidNegativeArmotizationCode
    }
}

case object NegativeAmortization extends NegativeAmortizationEnum {
  override val code: Int           = 1
  override val description: String = "Negative amortization"
}

case object NoNegativeAmortization extends NegativeAmortizationEnum {
  override val code: Int           = 2
  override val description: String = "No negative amortization"
}

case object NegativeAmortizationExempt extends NegativeAmortizationEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt Negative Amortization"
}

case object InvalidNegativeArmotizationCode extends NegativeAmortizationEnum {
  override def code: Int           = -1
  override def description: String = "Invalid Code"
}
