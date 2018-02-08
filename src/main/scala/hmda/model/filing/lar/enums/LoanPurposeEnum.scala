package hmda.model.filing.lar.enums

sealed trait LoanPurposeEnum {
  val code: Int
  val description: String
}

object LoanPurposeEnum {
  val values = List(1, 2, 31, 32, 4, 5)

  def valueOf(code: Int): LoanPurposeEnum = {
    code match {
      case 1  => HomePurchase
      case 2  => HomeImprovement
      case 31 => Refinancing
      case 32 => CashOutRefinancing
      case 4  => OtherPurpose
      case 5  => LoanPurposeNotApplicable
      case _  => throw new Exception("Invalid Loan Purpose Code")
    }
  }
}

case object HomePurchase extends LoanPurposeEnum {
  override val code: Int = 1
  override val description: String = "Home Purchase"
}

case object HomeImprovement extends LoanPurposeEnum {
  override val code: Int = 2
  override val description: String = "Home Improvement"
}

case object Refinancing extends LoanPurposeEnum {
  override val code: Int = 31
  override val description: String = "Refinancing"
}

case object CashOutRefinancing extends LoanPurposeEnum {
  override val code: Int = 32
  override val description: String = "Cash-out refinancing"
}

case object OtherPurpose extends LoanPurposeEnum {
  override val code: Int = 4
  override val description: String = "Other purpose"
}

case object LoanPurposeNotApplicable extends LoanPurposeEnum {
  override val code: Int = 5
  override val description: String = "Not applicable"
}
