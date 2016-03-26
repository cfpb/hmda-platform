package hmda.model.fi.ts

object Respondent {
  def empty: Respondent = {
    Respondent("", "", "", "", "", "")
  }
}

case class Respondent(
  id: String,
  name: String,
  address: String,
  city: String,
  state: String,
  zipCode: String
)

