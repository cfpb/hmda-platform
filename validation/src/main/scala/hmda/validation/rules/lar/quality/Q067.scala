package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Applicant, LoanApplicationRegister }
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q067 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q067"

  override def apply(lar: LoanApplicationRegister): Result = {
    val app = lar.applicant

    when(lar.actionTakenType is oneOf(1, 2, 3, 4, 5, 7, 8) and
      applicantNotNaturalPerson(app) and coApplicantNotNaturalPerson(app)) {
      app.income is equalTo("NA")
    }
  }

  private def applicantNotNaturalPerson(app: Applicant): Result = {
    app.ethnicity is equalTo(4) and
      (app.race1 is equalTo(7)) and
      (app.sex is equalTo(4))
  }

  private def coApplicantNotNaturalPerson(app: Applicant): Result = {
    app.coEthnicity is equalTo(4) and
      (app.coRace1 is equalTo(7)) and
      (app.coSex is equalTo(4))

  }
}
