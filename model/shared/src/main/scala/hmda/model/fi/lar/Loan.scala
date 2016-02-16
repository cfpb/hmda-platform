package hmda.model.fi.lar

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class Loan(
  id: String,
  applicationDate: String,
  loanType: Int,
  propertyType: Int,
  purpose: Int,
  occupancy: Int,
  amount: Int
)

