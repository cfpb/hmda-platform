package hmda.query.ts

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
    submissionId: Option[String] = Some("")
) {
  def isEmpty: Boolean = lei == ""

  def toPSV: String = {
    s"$id|$institutionName|$year|" +
      s"$quarter|$name|$phone|" +
      s"$email|$street|$city|" +
      s"$state|$zipCode|$agency|" +
      s"$totalLines|$taxId|$lei"

  }

  def toPublicPSV: String = {
    s"$year|$quarter|$lei|$taxId|$agency|" +
      s"$name|$city|$state|$zipCode|$totalLines"
  }

  def toPublicCSV: String = {
    s"$year,$quarter,$lei,$taxId,$agency," +
      s"$name,$city,$state,$zipCode,$totalLines"
  }
}
