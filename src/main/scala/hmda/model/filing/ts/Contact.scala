package hmda.model.filing.ts

case class Contact(
    name: String = "",
    phone: String = "",
    email: String = "",
    address: Address = Address()
) {
  def toCSV: String = {
    s"$name|$phone|$email|${address.toCSV}"
  }
}
