package hmda.model.filing.lar.enums

trait ManufacturedHomeSecuredPropertyEnum extends LarEnum

object ManufacturedHomeSecuredPropertyEnum
    extends LarCodeEnum[ManufacturedHomeSecuredPropertyEnum] {
  override val values = List(1, 2, 3)

  override def valueOf(code: Int): ManufacturedHomeSecuredPropertyEnum = {
    code match {
      case 1 => ManufacturedHomeAndLand
      case 2 => ManufacturedHomeAndNotLand
      case 3 => ManufacturedHomeSecuredNotApplicable
      case _ =>
        throw new Exception("Invalid Manufactured Home Secured Property Code")
    }
  }
}

case object ManufacturedHomeAndLand
    extends ManufacturedHomeSecuredPropertyEnum {
  override val code: Int = 1
  override val description: String = "Manufactured home and land"
}

case object ManufacturedHomeAndNotLand
    extends ManufacturedHomeSecuredPropertyEnum {
  override val code: Int = 2
  override val description: String = "Manufactured home and not land"
}

case object ManufacturedHomeSecuredNotApplicable
    extends ManufacturedHomeSecuredPropertyEnum {
  override val code: Int = 3
  override val description: String = "Not applicable"
}
