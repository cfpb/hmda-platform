package hmda.model.filing.lar

sealed trait LienStatus {
  val code: Int
  val description: String
}

object LienStatus {
  val values = List(1,2)

  def valueOf(code: Int): LienStatus = {
    code match {
      case 1 => SecuredByFirstLien
      case 2 => SecuredBySubordinateLien
      case _ => throw new Exception("Invalid Lien Status Code")
    }
  }
}

case object SecuredByFirstLien extends LienStatus {
  override val code: Int = 1
  override val description: String = "Secured by a first lien"
}

case object SecuredBySubordinateLien extends LienStatus {
  override val code: Int = 2
  override val description: String = "Secured by a subordinate lien"
}
