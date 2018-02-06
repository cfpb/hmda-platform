package hmda.model.filing.lar

sealed trait HOEPAStatus {
  val code: Int
  val description: String
}

object HOEPAStatus {
  val values = List(1,2,3)

  def valueOf(code: Int): HOEPAStatus = {
    code match {
      case 1 => HighCostMortgage
      case 2 => NotHighCostMortgage
      case 3 => HOEPStatusANotApplicable
      case _ => throw new Exception("Invalid HOEPA Status Code")
    }
  }
}

case object HighCostMortgage extends HOEPAStatus {
  override val code: Int = 1
  override val description: String = "High-cost mortgage"
}

case object NotHighCostMortgage extends HOEPAStatus {
  override val code: Int = 2
  override val description: String = "Not a high-cost mortgage"
}

case object HOEPStatusANotApplicable extends HOEPAStatus {
  override val code: Int = 3
  override val description: String = "Not applicable"
}
