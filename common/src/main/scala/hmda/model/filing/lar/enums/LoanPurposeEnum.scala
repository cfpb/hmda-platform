package hmda.model.filing.lar.enums

sealed trait LoanPurposeEnum extends LarEnum

object LoanPurposeEnum extends LarCodeEnum[LoanPurposeEnum] {
  override val values = List(1, 2, 31, 32, 4, 5)

  override def valueOf(code: Int): LoanPurposeEnum =
    code match {
      case 1  => HomePurchase
      case 2  => HomeImprovement
      case 31 => Refinancing
      case 32 => CashOutRefinancing
      case 4  => OtherPurpose
      case 5  => LoanPurposeNotApplicable
      case other  => new InvalidLoanPurposeCode(other)
    }
}

case object HomePurchase extends LoanPurposeEnum {
  override val code: Int           = 1
  override val description: String = "Home Purchase"
}

case object HomeImprovement extends LoanPurposeEnum {
  override val code: Int           = 2
  override val description: String = "Home Improvement"
}

case object Refinancing extends LoanPurposeEnum {
  override val code: Int           = 31
  override val description: String = "Refinancing"
}

case object CashOutRefinancing extends LoanPurposeEnum {
  override val code: Int           = 32
  override val description: String = "Cash-out refinancing"
}

case object OtherPurpose extends LoanPurposeEnum {
  override val code: Int           = 4
  override val description: String = "Other purpose"
}

case object LoanPurposeNotApplicable extends LoanPurposeEnum {
  override val code: Int           = 5
  override val description: String = "Not applicable"
}

case object InvalidLoanPurposeExemptCode extends LoanPurposeEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidLoanPurposeCode(value: Int = -1) extends LoanPurposeEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidLoanPurposeCode => true
            case _ => false
        }
}
