package hmda.model.fi.ts

import scalajs.js
import scalajs.js.annotation.ScalaJSDefined

@ScalaJSDefined
trait TransmittalSheetJS extends js.Object {
  val id: Int
  val agencyCode: Int
  val timestamp: String
  val activityYear: Int
  val taxId: String
  val totalLines: Int
  val respondent: RespondentJS
  val parent: ParentJS
  val contact: ContactJS
  val respondentId: String
}

@ScalaJSDefined
trait RespondentJS extends js.Object {
  val id: String
  val name: String
  val address: String
  val city: String
  val state: String
  val zipCode: String
}

@ScalaJSDefined
trait ParentJS extends js.Object {
  val name: String
  val address: String
  val city: String
  val state: String
  val zipCode: String
}

@ScalaJSDefined
trait ContactJS extends js.Object {
  val name: String
  val phone: String
  val fax: String
  val email: String
}
