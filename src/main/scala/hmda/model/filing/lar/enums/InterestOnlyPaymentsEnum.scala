package hmda.model.filing.lar.enums

sealed trait InterestOnlyPaymentsEnum extends LarEnum

object InterestOnlyPaymentsEnum extends LarCodeEnum[InterestOnlyPaymentsEnum] {
  override val values = List(1, 2)

  override def valueOf(code: Int): InterestOnlyPaymentsEnum = {
    code match {
      case 1 => InterestOnlyPayment
      case 2 => NoInterestOnlyPayment
      case _ => throw new Exception("Invalid Interest Only Payment Code")
    }
  }
}

case object InterestOnlyPayment extends InterestOnlyPaymentsEnum {
  override val code: Int = 1
  override val description: String = "Interest-only payments"
}

case object NoInterestOnlyPayment extends InterestOnlyPaymentsEnum {
  override val code: Int = 2
  override val description: String = "No interest-only payments"
}
