package hmda.model.filing.lar.enums

trait ManufacturedHomeSecuredPropertyEnum extends LarEnum

object ManufacturedHomeSecuredPropertyEnum
    extends LarCodeEnum[ManufacturedHomeSecuredPropertyEnum] {
  override val values = List(1, 2, 3, 1111)

  override def valueOf(code: Int): ManufacturedHomeSecuredPropertyEnum = {
    code match {
      case 1    => ManufacturedHomeAndLand
      case 2    => ManufacturedHomeAndNotLand
      case 3    => ManufacturedHomeSecuredNotApplicable
      case 1111 => ManufacturedHomeSecuredExempt
      case _    => InvalidManufacturedHomeSecuredPropertyCode
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

case object ManufacturedHomeSecuredExempt
    extends ManufacturedHomeSecuredPropertyEnum {
  override def code: Int = 1111
  override def description: String = "Exempt Manufactured Home Secured"
}

case object InvalidManufacturedHomeSecuredPropertyCode
    extends ManufacturedHomeSecuredPropertyEnum {
  override def code: Int = -1
  override def description: String = "Invalid Code"
}
