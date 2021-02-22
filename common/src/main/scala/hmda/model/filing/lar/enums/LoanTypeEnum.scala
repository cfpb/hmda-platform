package hmda.model.filing.lar.enums

sealed trait LoanTypeEnum extends LarEnum

object LoanTypeEnum extends LarCodeEnum[LoanTypeEnum] {
  override val values = List(1, 2, 3, 4)

  override def valueOf(code: Int): LoanTypeEnum =
    code match {
      case 1 => Conventional
      case 2 => FHAInsured
      case 3 => VAGuaranteed
      case 4 => RHSOrFSAGuaranteed
      case other => new InvalidLoanTypeCode(other)
    }
}

case object Conventional extends LoanTypeEnum {
  override val code: Int = 1
  override val description: String =
    "Conventional (not insured or guaranteed by FHA, VA, RHS, or FSA)"
}

case object FHAInsured extends LoanTypeEnum {
  override val code: Int = 2
  override val description: String =
    "Federal Housing Administration insured (FHA)"
}

case object VAGuaranteed extends LoanTypeEnum {
  override val code: Int           = 3
  override val description: String = "Veterans Affairs guaranteed (VA)"
}

case object RHSOrFSAGuaranteed extends LoanTypeEnum {
  override val code: Int = 4
  override val description: String =
    "USDA Rural Housing Service or Farm Service Agency guaranteed (RHS or FSA)"
}

case object InvalidLoanTypeExemptCode extends LoanTypeEnum {
  override val code: Int = 1111
  override val description: String =
    "Invalid exemption code for loan field."
}

class InvalidLoanTypeCode(value: Int = -1) extends LoanTypeEnum {
  override def code: Int           = value
  override def description: String = "Invalid Code"
  override def equals(that: Any): Boolean =
        that match {
            case that: InvalidLoanTypeCode => true
            case _ => false
        }
}
