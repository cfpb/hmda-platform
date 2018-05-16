package hmda.model.institution

sealed trait InstitutionType {
  val code: Int
  val name: String
}

object InstitutionType {
  def apply(): InstitutionType = UndeterminedInstitutionType

  def valueOf(code: Int): InstitutionType = code match {
    case -1 => UndeterminedInstitutionType
    case _  => throw new Exception("Invalid Institution Type")
  }
}

case object NationalBank extends InstitutionType {
  override val code: Int = 1
  override val name: String = "National Bank"
}

case object StateMemberBank extends InstitutionType {
  override val code: Int = 2
  override val name: String = "State Member Bank"
}

case object StateNonMemberBank extends InstitutionType {
  override val code: Int = 3
  override val name: String = "State Non Member Bank"
}

case object StateCharteredThrift extends InstitutionType {
  override val code: Int = 4
  override val name: String = "State Chartered Thrift"
}

case object FederalCharteredThrift extends InstitutionType {
  override val code: Int = 5
  override val name: String = "Federal Chartered Thrift"
}

case object CreditUnion extends InstitutionType {
  override val code: Int = 6
  override val name: String = "Credit Union"
}

case object FBOFederalBranchOrAgency extends InstitutionType {
  override val code: Int = 7
  override val name: String = "Federal Branch or Agency of Foreign Banking Organizations"
}

case object ForeignBankBranchOrAgency extends InstitutionType {
  override val code: Int = 8
  override val name: String = "Branch or Agency of Foreign Bank"
}

case object MBSOfNationalBank extends InstitutionType {
  override val code: Int = 9
  override val name: String = "Mortgage banking subsidiary of National Bank"
}

case object MBSOfStateMemberBank extends InstitutionType {
  override val code: Int = 10
  override val name: String = "Mortgate banking subsidiary of State Member Bank"
}

case object MBSOfStateNonMemberBank extends InstitutionType {
  override val code: Int = 11
  override val name: String = "Mortgage banking subsidiary of State Non Member Bank"
}

case object MBSOfBankHoldingCompany extends InstitutionType {
  override val code: Int = 12
  override val name: String = "Mortgage banking subsidiary of Bank Holding Company"
}

case object MBSOfCreditUnion extends InstitutionType {
  override val code: Int = 13
  override val name: String = "Mortgate banking subsidiary of Credit Union"
}

case object IndependentMBS extends InstitutionType {
  override val code: Int = 14
  override val name: String = "Independent Mortgate banking subsidiary"
}

case object MBSOfSavingsAndLoanHoldingCompany extends InstitutionType {
  override val code: Int = 15
  override val name: String = "Mortgate banking subsidiary of Savings and Loan Holding Company"
}

case object MBSOfStateCharteredThrift extends InstitutionType {
  override val code: Int = 16
  override val name: String = "Mortgate banking subsidiary of State Chartered Thrift"
}

case object MBSOfFederalCharteredThrift extends InstitutionType {
  override val code: Int = 17
  override val name: String = "Mortgate banking subsidiary of Federal Chartered Thrift"
}

case object Affiliate extends InstitutionType {
  override val code: Int = 18
  override val name: String = "Affiliate"
}

case object UndeterminedInstitutionType extends InstitutionType {
  override val code: Int = -1
  override val name: String = "Undetermined Institution Type"
}
