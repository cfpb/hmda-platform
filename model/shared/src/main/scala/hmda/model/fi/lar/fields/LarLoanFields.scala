package hmda.model.fi.lar.fields

case object Id extends LarField {
  override def name = "Loan/Application Number"

  override def description = ""
}

case object ApplicationDate extends LarField {
  override def name = "Date Application Received"

  override def description = ""
}

case object LoanType extends LarField {
  override def name = "Loan Type"

  override def description = ""
}

case object PropertyType extends LarField {
  override def name = "Property Type"

  override def description = ""
}

case object Purpose extends LarField {
  override def name = "Loan Purpose"

  override def description = ""
}

case object Occupancy extends LarField {
  override def name = "Owner Occupancy"

  override def description = ""
}

case object Amount extends LarField {
  override def name = "Loan Amount"

  override def description = ""
}
