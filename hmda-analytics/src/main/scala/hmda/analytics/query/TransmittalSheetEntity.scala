package hmda.analytics.query

case class TransmittalSheetEntity(
    lei: String = "",
    id: Int = 0,
    institutionName: String = "",
    year: Int = 0,
    quarter: Int = 0,
    name: String = "",
    phone: String = "",
    email: String = "",
    street: String = "",
    city: String = "",
    state: String = "",
    zipCode: String = "",
    agency: Int = 0,
    totalLines: Int = 0,
    taxId: String = "",
    submissionId: String = ""
) {
  def isEmpty: Boolean = lei == ""
}
