package hmda.model.fi.ts

case class Respondent(
  id: String,
  name: String,
  address: String,
  city: String,
  state: String,
  zipCode: String
) extends NameAndAddress

object Respondent {
  def apply(): Respondent = {
    Respondent("", "", "", "", "", "")
  }
}

