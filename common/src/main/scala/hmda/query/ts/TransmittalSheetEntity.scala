package hmda.query.ts

import hmda.util.conversion.ColumnDataFormatter

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
  submissionId: Option[String] = Some(""),
  createdAt: Option[java.sql.Timestamp] = Some(new java.sql.Timestamp(0))

)extends ColumnDataFormatter {
  def isEmpty: Boolean = lei == ""

  def toRegulatorPSV: String =
    s"$id|$institutionName|$year|" +
      s"$quarter|$name|$phone|" +
      s"$email|$street|$city|" +
      s"$state|$zipCode|$agency|" +
      s"$totalLines|$taxId|$lei|${dateToString(createdAt)}"

  def toPublicPSV: String =
    s"$year|$quarter|$lei|$taxId|$agency|" +
      s"$institutionName|$state|$city|$zipCode|$totalLines"

}



