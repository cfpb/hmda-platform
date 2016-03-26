package hmda.model.fi.ts

object Contact {
  def empty: Contact = {
    Contact("", "", "", "")
  }
}

case class Contact(
  name: String,
  phone: String,
  fax: String,
  email: String
)
