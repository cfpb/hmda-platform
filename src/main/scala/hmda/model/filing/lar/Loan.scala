package hmda.model.filing.lar

case class Loan(
    ULI: String,
    applicationDate: String,
    loanType: LoanType,
    loanPurpose: LoanPurpose,
    constructionMethod: ConstructionMethod,
    occupancy: OccupancyType,
    amount: Double
)
