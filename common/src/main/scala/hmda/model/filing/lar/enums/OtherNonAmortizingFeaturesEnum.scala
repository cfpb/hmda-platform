package hmda.model.filing.lar.enums

trait OtherNonAmortizingFeaturesEnum extends LarEnum

object OtherNonAmortizingFeaturesEnum
    extends LarCodeEnum[OtherNonAmortizingFeaturesEnum] {
  override val values = List(1, 2, 1111)

  override def valueOf(code: Int): OtherNonAmortizingFeaturesEnum =
    code match {
      case 1    => OtherNonFullyAmortizingFeatures
      case 2    => NoOtherNonFullyAmortizingFeatures
      case 1111 => OtherNonAmortizingFeaturesExempt
      case other    => new InvalidOtherNonAmortizingFeaturesCode(other)
    }
}

case object OtherNonFullyAmortizingFeatures extends OtherNonAmortizingFeaturesEnum {
  override val code: Int           = 1
  override val description: String = "Other non-fully amortizing features"
}

case object NoOtherNonFullyAmortizingFeatures extends OtherNonAmortizingFeaturesEnum {
  override val code: Int           = 2
  override val description: String = "No other non-fully amortizing features"
}

case object OtherNonAmortizingFeaturesExempt extends OtherNonAmortizingFeaturesEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt Other Non Amortizing Features"
}

class InvalidOtherNonAmortizingFeaturesCode(value: Int = -1) extends OtherNonAmortizingFeaturesEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidOtherNonAmortizingFeaturesCode => true
            case _ => false
        }
}
