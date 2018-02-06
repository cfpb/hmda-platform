package hmda.model.filing.lar

sealed trait OccupancyType {
  val code: Int
  val description: String
}

object OccupancyType {
  val values = List(1, 2, 3)

  def valueOf(code: Int): OccupancyType = {
    code match {
      case 1 => PrincipalResidence
      case 2 => SecondResidence
      case 3 => InvestmentProperty
      case _ => throw new Exception("Invalid Occupancy Type Code")
    }
  }
}

case object PrincipalResidence extends OccupancyType {
  override val code: Int = 1
  override val description: String = "Principal Residence"
}

case object SecondResidence extends OccupancyType {
  override val code: Int = 2
  override val description: String = "Second Residence"
}

case object InvestmentProperty extends OccupancyType {
  override val code: Int = 3
  override val description: String = "Investment Property"
}
