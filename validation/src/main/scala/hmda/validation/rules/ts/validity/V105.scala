package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

/*
 Respondent name, address, city, state, and zip code must not = blank
 */
object V105 extends EditCheck[TransmittalSheet] {
  def apply(ts: TransmittalSheet): Result = {
    val respondent = ts.respondent
    val respName = respondent.name
    val respAddress = respondent.address
    val respCity = respondent.city
    val respState = respondent.state
    val respZipCode = respondent.zipCode

    (respName not empty) and
      (respAddress not empty) and
      (respCity not empty) and
      (respState not empty) and
      (respZipCode not empty)
  }

  override def name: String = "V105"
}
