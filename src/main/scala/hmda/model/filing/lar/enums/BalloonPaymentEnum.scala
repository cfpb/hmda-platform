package hmda.model.filing.lar.enums

sealed trait BalloonPaymentEnum extends LarEnum

object BalloonPaymentEnum extends LarCodeEnum[BalloonPaymentEnum] {
  override val values = List(1, 2)

  override def valueOf(code: Int): BalloonPaymentEnum = {
    code match {
      case 1 => BalloonPayment
      case 2 => NoBallonPayment
      case _ => throw new Exception("Invalid Balloon Payment Code")
    }
  }
}

case object BalloonPayment extends BalloonPaymentEnum {
  override val code: Int = 1
  override val description: String = "Balloon Payment"
}

case object NoBallonPayment extends BalloonPaymentEnum {
  override val code: Int = 2
  override val description: String = "No Balloon Payment"
}
