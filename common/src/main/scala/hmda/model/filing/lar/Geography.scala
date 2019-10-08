package hmda.model.filing.lar

import hmda.model.filing.PipeDelimited

case class Geography(street: String = "",
                     city: String = "",
                     state: String = "",
                     zipCode: String = "",
                     county: String = "",
                     tract: String = "")
    extends PipeDelimited {
  override def toCSV: String =
    s"$street|$city|$state|$zipCode|$county|$tract"
}
