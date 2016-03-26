package hmda.model.fi.ts

object Parent {
  def empty: Parent = {
    Parent("", "", "", "", "")
  }
}

case class Parent(
  name: String,
  address: String,
  city: String,
  state: String,
  zipCode: String
)

