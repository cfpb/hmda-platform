package hmda.model.filing.lar.enums

sealed trait InterestOnlyPaymentsEnum extends LarEnum

object InterestOnlyPaymentsEnum extends LarCodeEnum[InterestOnlyPaymentsEnum] {
  override val values = List(1, 2, 1111)

  override def valueOf(code: Int): InterestOnlyPaymentsEnum =
    code match {
      case 1    => InterestOnlyPayment
      case 2    => NoInterestOnlyPayment
      case 1111 => InterestOnlyPaymentExempt
      case other    => new InvalidInterestOnlyPaymentCode(other)
    }
}

case object InterestOnlyPayment extends InterestOnlyPaymentsEnum {
  override val code: Int           = 1
  override val description: String = "Interest-only payments"
}

case object NoInterestOnlyPayment extends InterestOnlyPaymentsEnum {
  override val code: Int           = 2
  override val description: String = "No interest-only payments"
}

case object InterestOnlyPaymentExempt extends InterestOnlyPaymentsEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt Interest Only Payments"
}

class InvalidInterestOnlyPaymentCode(value: Int = -1) extends InterestOnlyPaymentsEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidInterestOnlyPaymentCode => true
            case _ => false
        }
}
