package hmda.model.institution

sealed trait Agency {
  def value: Int
  val name: String
  val fullName: String
}

object Agency {
  def valueOf(code: Int): Agency = {
    code match {
      case 1 => OCC
      case 2 => FRS
      case 3 => FDIC
      case 5 => NCUA
      case 7 => HUD
      case 9 => CFPB
      case _ => UndeterminedAgency
    }
  }
}

case object OCC extends Agency {
  override def value = 1

  override val name = "occ"
  override val fullName = "Office of the Comptroller of the Currency"
}

case object FRS extends Agency {
  override def value = 2

  override val name = "frs"
  override val fullName = "Federal Reserve System"
}

case object FDIC extends Agency {
  override def value = 3

  override val name = "fdic"
  override val fullName = "Federal Deposit Insurance Corporation"
}

case object NCUA extends Agency {
  override def value = 5

  override val name = "ncua"
  override val fullName = "National Credit Union Administration"
}

case object HUD extends Agency {
  override def value = 7

  override val name = "hud"
  override val fullName = "Housing and Urban Development"
}

case object CFPB extends Agency {
  override def value = 9

  override val name = "cfpb"
  override val fullName = "Consumer Financial Protection Bureau"
}

case object UndeterminedAgency extends Agency {
  override def value = -1

  override val name = "undetermined"
  override val fullName = "Undetermined Agency"
}
