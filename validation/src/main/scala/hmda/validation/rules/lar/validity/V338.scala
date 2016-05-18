package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V338 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V338"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(applicantOk(lar)) {
      lar.applicant.income is equalTo("NA")
    }
  }

  private def applicantOk(lar: LoanApplicationRegister): Result = {
    val okAction = List(1, 2, 3, 4, 5, 7, 8)

    (lar.applicant.ethnicity is equalTo(4)) and
      (lar.applicant.race1 is equalTo(7)) and
      (lar.applicant.sex is equalTo(4)) and
      (lar.applicant.coEthnicity is equalTo(5)) and
      (lar.applicant.coRace1 is equalTo(8)) and
      (lar.applicant.coSex is equalTo(5)) and
      (lar.actionTakenType is containedIn(okAction))
  }
}
