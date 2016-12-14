package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Applicant, LoanApplicationRegister }
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.ApplicantUtils

object Q067 extends EditCheck[LoanApplicationRegister] with ApplicantUtils {
  override def name: String = "Q067"

  override def apply(lar: LoanApplicationRegister): Result = {
    val app = lar.applicant

    when(lar.actionTakenType is oneOf(1, 2, 3, 4, 5, 7, 8) and
      applicantNotNaturalPerson(app) and coApplicantNotNaturalPerson(app)) {
      app.income is equalTo("NA")
    }
  }

}
