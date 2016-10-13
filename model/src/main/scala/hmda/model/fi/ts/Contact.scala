package hmda.model.fi.ts

case class Contact(
  name: String,
  phone: String,
  fax: String,
  email: String
)

object Contact {
  def apply(): Contact = {
    Contact("", "", "", "")
  }
}
