package hmda.model.filing.lar

sealed trait LoanType {
  val code: Int
  val description: String
}

object LoanType {
  val values = List(1, 2, 3, 4)

  def valueOf(code: Int): LoanType = {
    code match {
      case 1 => Conventional
      case 2 => FHAInsured
      case 3 => VAGuaranteed
      case 4 => RHSOrFSAGuaranteed
      case _ => throw new Exception("Invalid Loan Type Code")
    }
  }
}

case object Conventional extends LoanType {
  override val code: Int = 1

  override val description: String =
    "Conventional (not insured or guaranteed by FHA, VA, RHS, or FSA)"
}

case object FHAInsured extends LoanType {
  override val code: Int = 2

  override val description: String =
    "Federal Housing Administration insured (FHA)"
}

case object VAGuaranteed extends LoanType {
  override val code: Int = 3

  override val description: String = "Veterans Affairs guaranteed (VA)"
}

case object RHSOrFSAGuaranteed extends LoanType {
  override val code: Int = 4

  override val description: String =
    "USDA Rural Housing Service or Farm Service Agency guaranteed (RHS or FSA)"
}
