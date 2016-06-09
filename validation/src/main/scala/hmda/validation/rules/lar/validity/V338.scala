package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.ApplicantUtils

object V338 extends EditCheck[LoanApplicationRegister] with ApplicantUtils {
  override def name: String = "V338"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(notANaturalPerson(lar)) {
      lar.applicant.income is equalTo("NA")
    }
  }

  private def notANaturalPerson(lar: LoanApplicationRegister): Result = {
    val okAction = List(1, 2, 3, 4, 5, 7, 8)

    applicantNotNaturalPerson(lar.applicant) and
      (lar.applicant.coEthnicity is equalTo(5)) and
      (lar.applicant.coRace1 is equalTo(8)) and
      (lar.applicant.coSex is equalTo(5)) and
      (lar.actionTakenType is containedIn(okAction))
  }
}
