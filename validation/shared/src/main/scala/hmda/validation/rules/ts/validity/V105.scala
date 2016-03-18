package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.Respondent
import hmda.validation.dsl.{ CommonDsl, Result }

/*
 Respondent name, address, city, state, and zip code must not = blank
 */
object V105 extends CommonDsl {
  def apply(respondent: Respondent): Result = {
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

}
