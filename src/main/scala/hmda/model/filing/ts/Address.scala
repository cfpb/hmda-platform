package hmda.model.filing.ts

case class Address(
    street: String = "",
    city: String = "",
    state: String = "",
    zipCode: String = ""
) {
  def toCSV: String = {
    s"$street|$city|$state|$zipCode"
  }
}
