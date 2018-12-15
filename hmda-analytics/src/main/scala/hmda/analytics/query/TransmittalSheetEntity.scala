package hmda.analytics.query

case class TransmittalSheetEntity(
    lei: String,
    id: Int,
    institutionName: String,
    year: Int,
    quarter: Int,
    name: String,
    phone: String,
    email: String,
    street: String,
    city: String,
    state: String,
    zipCode: String,
    agency: Int,
    totalLines: Int,
    taxId: String,
) {
  def isEmpty: Boolean = lei == ""
}
