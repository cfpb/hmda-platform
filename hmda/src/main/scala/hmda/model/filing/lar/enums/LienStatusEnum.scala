package hmda.model.filing.lar.enums

sealed trait LienStatusEnum extends LarEnum

object LienStatusEnum extends LarCodeEnum[LienStatusEnum] {
  override val values = List(1, 2)

  override def valueOf(code: Int): LienStatusEnum = {
    code match {
      case 1 => SecuredByFirstLien
      case 2 => SecuredBySubordinateLien
      case _ => InvalidLienStatusCode
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

case object InvalidLienStatusCode extends LienStatusEnum {
  override def code: Int = -1
  override def description: String = "Invalid Code"
}
