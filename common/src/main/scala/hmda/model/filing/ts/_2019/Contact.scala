package hmda.model.filing.ts._2019

import hmda.model.filing.PipeDelimited

case class Contact(
    name: String = "",
    phone: String = "",
    email: String = "",
    address: Address = Address()
) extends PipeDelimited {
  override def toCSV: String = {
    s"$name|$phone|$email|${address.toCSV}"
  }
}
