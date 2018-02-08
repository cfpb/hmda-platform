package hmda.model.filing.lar

import hmda.model.filing.lar.enums.{
  ConstructionMethodEnum,
  LoanPurposeEnum,
  LoanTypeEnum,
  OccupancyEnum
}

case class Loan(
    ULI: String,
    applicationDate: String,
    loanType: LoanTypeEnum,
    loanPurpose: LoanPurposeEnum,
    constructionMethod: ConstructionMethodEnum,
    occupancy: OccupancyEnum,
    amount: Double
)
