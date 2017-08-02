package hmda.model.fi.ts

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class Respondent(
  id: String,
  name: String,
  address: String,
  city: String,
  state: String,
  zipCode: String
) extends NameAndAddress

