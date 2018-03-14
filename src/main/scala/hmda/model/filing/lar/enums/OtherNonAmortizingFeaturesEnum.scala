package hmda.model.filing.lar.enums

trait OtherNonAmortizingFeaturesEnum extends LarEnum

object OtherNonAmortizingFeaturesEnum
    extends LarCodeEnum[OtherNonAmortizingFeaturesEnum] {
  override val values = List(1, 2)

  override def valueOf(code: Int): OtherNonAmortizingFeaturesEnum = {
    code match {
      case 1 => OtherNonFullyAmortizingFeatures
      case 2 => NoOtherNonFullyAmortizingFeatures
      case _ => InvalidOtherNonAmortizingFeaturesCode
    }
  }
}

case object OtherNonFullyAmortizingFeatures
    extends OtherNonAmortizingFeaturesEnum {
  override val code: Int = 1
  override val description: String = "Other non-fully amortizing features"
}

case object NoOtherNonFullyAmortizingFeatures
    extends OtherNonAmortizingFeaturesEnum {
  override val code: Int = 2
  override val description: String = "No other non-fully amortizing features"
}

case object InvalidOtherNonAmortizingFeaturesCode
    extends OtherNonAmortizingFeaturesEnum {
  override def code: Int = -1
  override def description: String = "Invalid Code"
}
