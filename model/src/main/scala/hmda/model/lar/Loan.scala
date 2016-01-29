package hmda.model.lar

case class Loan(
  id: String,
  applicationDate: String,
  loanType: Int,
  propertyType: Int,
  purpose: Int,
  occupancy: Int,
  amount: Int
)

