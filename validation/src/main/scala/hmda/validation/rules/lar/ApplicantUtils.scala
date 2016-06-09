package hmda.validation.rules.lar

import hmda.model.fi.lar.Applicant
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

trait ApplicantUtils {
  def applicantNotNaturalPerson(app: Applicant): Result = {
    app.ethnicity is equalTo(4) and
      (app.race1 is equalTo(7)) and
      (app.sex is equalTo(4))
  }

  def coApplicantNotNaturalPerson(app: Applicant): Result = {
    app.coEthnicity is equalTo(4) and
      (app.coRace1 is equalTo(7)) and
      (app.coSex is equalTo(4))
  }

  def coApplicantDoesNotExist(app: Applicant): Result = {
    app.coEthnicity is equalTo(5) and
      (app.coRace1 is equalTo(8)) and
      (app.coSex is equalTo(5))
  }
}
