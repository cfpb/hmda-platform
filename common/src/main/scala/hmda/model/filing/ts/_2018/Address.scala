package hmda.model.filing.ts._2018

import hmda.model.filing.PipeDelimited

case class Address(
    street: String = "",
    city: String = "",
    state: String = "",
    zipCode: String = ""
) extends PipeDelimited {
  override def toCSV: String = {
    s"$street|$city|$state|$zipCode"
  }
}
