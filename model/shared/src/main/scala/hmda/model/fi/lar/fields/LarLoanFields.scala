package hmda.model.fi.lar.fields

import hmda.model.fi.RecordField

object LarLoanFields {

  val id = RecordField("Loan/Application Number", "")

  val applicationDate = RecordField("Date Application Received", "")

  val loanType = RecordField("Loan Type", "")

  val propertyType = RecordField("Property Type", "")

  val purpose = RecordField("Loan Purpose", "")

  val occupancy = RecordField("Owner Occupancy", "")

  val amount = RecordField("Loan Amount", "")
}
