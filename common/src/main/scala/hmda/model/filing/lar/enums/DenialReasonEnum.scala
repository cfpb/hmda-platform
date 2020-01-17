package hmda.model.filing.lar.enums

sealed trait DenialReasonEnum extends LarEnum

object DenialReasonEnum extends LarCodeEnum[DenialReasonEnum] {
  val values = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1111)

  def valueOf(code: Int): DenialReasonEnum =
    code match {
      case 0    => EmptyDenialValue
      case 1    => DebtToIncomeRatio
      case 2    => EmploymentHistory
      case 3    => CreditHistory
      case 4    => Collateral
      case 5    => InsufficientCash
      case 6    => UnverifiableInformation
      case 7    => CreditApplicationIncomplete
      case 8    => MortgageInsuranceDenied
      case 9    => OtherDenialReason
      case 10   => DenialReasonNotApplicable
      case 1111 => ExemptDenialReason
      case other    => new InvalidDenialReasonCode(other)
    }
}

case object EmptyDenialValue extends DenialReasonEnum {
  override def code: Int           = 0
  override def description: String = "Empty Value"
}

case object DebtToIncomeRatio extends DenialReasonEnum {
  override val code: Int           = 1
  override val description: String = "Debt-to-income ratio"
}

case object EmploymentHistory extends DenialReasonEnum {
  override val code: Int           = 2
  override val description: String = "Employment history"
}

case object CreditHistory extends DenialReasonEnum {
  override val code: Int           = 3
  override val description: String = "Credit history"
}

case object Collateral extends DenialReasonEnum {
  override val code: Int           = 4
  override val description: String = "Collateral"
}

case object InsufficientCash extends DenialReasonEnum {
  override val code: Int = 5
  override val description: String =
    "Insufficient cash (downpayment, closing costs)"
}

case object UnverifiableInformation extends DenialReasonEnum {
  override val code: Int           = 6
  override val description: String = "Unverifiable information"
}

case object CreditApplicationIncomplete extends DenialReasonEnum {
  override val code: Int           = 7
  override val description: String = "Credit application incomplete"
}

case object MortgageInsuranceDenied extends DenialReasonEnum {
  override val code: Int           = 8
  override val description: String = "Mortgage insurance denied"
}

case object OtherDenialReason extends DenialReasonEnum {
  override val code: Int           = 9
  override val description: String = "Other"
}

case object DenialReasonNotApplicable extends DenialReasonEnum {
  override val code: Int           = 10
  override val description: String = "Not Applicable"
}

case object ExemptDenialReason extends DenialReasonEnum {
  override def code: Int           = 1111
  override def description: String = "Exempt Denial Reason"
}

class InvalidDenialReasonCode(value: Int = -1) extends DenialReasonEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidDenialReasonCode => true
            case _ => false
        }
}
