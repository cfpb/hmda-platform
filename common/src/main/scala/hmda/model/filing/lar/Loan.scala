package hmda.model.filing.lar

import hmda.model.filing.lar.enums._

case class Loan(
    ULI: String = "",
    applicationDate: String = "",
    loanType: LoanTypeEnum = InvalidLoanTypeCode,
    loanPurpose: LoanPurposeEnum = InvalidLoanPurposeCode,
    constructionMethod: ConstructionMethodEnum = InvalidConstructionMethodCode,
    occupancy: OccupancyEnum = InvalidOccupancyCode,
    amount: Double = 0.0,
    loanTerm: String = "NA",
    rateSpread: String = "NA",
    interestRate: String = "NA",
    prepaymentPenaltyTerm: String = "NA",
    debtToIncomeRatio: String = "NA",
    combinedLoanToValueRatio: String = "NA",
    introductoryRatePeriod: String = "NA"
)
