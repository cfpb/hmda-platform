package hmda.model.filing.lar

sealed trait ConstructionMethod {
  val code: Int
  val description: String
}

object ConstructionMethod {
  val values = List(1, 2, 3)

  def valueOf(code: Int): ConstructionMethod = {
    code match {
      case 1 => SiteBuilt
      case 2 => ManufacturedHome
      case _ => throw new Exception("Invalid Construction Method Code")
    }
  }
}

case object SiteBuilt extends ConstructionMethod {
  override val code: Int = 1
  override val description: String = "Site-built"
}

case object ManufacturedHome extends ConstructionMethod {
  override val code: Int = 2
  override val description: String = "Manufactured Home"
}
