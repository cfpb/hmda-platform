package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.ApplicantUtils

object Q051 extends EditCheck[LoanApplicationRegister] with ApplicantUtils {
  override def apply(lar: LoanApplicationRegister): Result = {
    when(applicantNotNaturalPerson(lar.applicant) and (lar.actionTakenType is equalTo(6))) {
      lar.hoepaStatus not equalTo(1)
    }
  }

  override def name = "Q051"
}
