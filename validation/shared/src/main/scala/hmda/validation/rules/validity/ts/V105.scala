package hmda.validation.rules.validity.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Result, CommonDsl }

/*
 Respondent name, address, city, state, and zip code must not = blank
 */
object V105 extends CommonDsl {
  def apply(ts: TransmittalSheet): Result = {
    val respName = ts.respondent.name
    val respAddress = ts.respondent.address
    val respCity = ts.respondent.city
    val respState = ts.respondent.state
    val respZipCode = ts.respondent.zipCode

    (respName not equalTo("")) and
      (respAddress not equalTo("")) and
      (respCity not equalTo("")) and
      (respState not equalTo("")) and
      (respZipCode not equalTo(""))

  }
}
