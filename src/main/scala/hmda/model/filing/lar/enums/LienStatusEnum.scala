package hmda.model.filing.lar.enums

sealed trait LienStatusEnum {
  val code: Int
  val description: String
}

object LienStatusEnum {
  val values = List(1, 2)

  def valueOf(code: Int): LienStatusEnum = {
    code match {
      case 1 => SecuredByFirstLien
      case 2 => SecuredBySubordinateLien
      case _ => throw new Exception("Invalid Lien Status Code")
    }
  }
}

case object SecuredByFirstLien extends LienStatusEnum {
  override val code: Int = 1
  override val description: String = "Secured by a first lien"
}

case object SecuredBySubordinateLien extends LienStatusEnum {
  override val code: Int = 2
  override val description: String = "Secured by a subordinate lien"
}
