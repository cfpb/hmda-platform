package hmda.model.filing.lar.enums

trait OtherNonAmortizingFeaturesEnum {
  val code: Int
  val description: String
}

object OtherNonAmortizingFeaturesEnum {
  val values = List(1, 2)

  def valueOf(code: Int): OtherNonAmortizingFeaturesEnum = {
    code match {
      case 1 => OtherNonFullyAmortizingFeatures
      case 2 => NoOtherNonFullyAmortizingFeatures
      case _ =>
        throw new Exception("Invalid Other Non Amortizing Features Code")
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
