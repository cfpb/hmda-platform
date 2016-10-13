package hmda.model.fi.ts

case class Parent(
  name: String,
  address: String,
  city: String,
  state: String,
  zipCode: String
) extends NameAndAddress

object Parent {
  def apply(): Parent = {
    Parent("", "", "", "", "")
  }
}

