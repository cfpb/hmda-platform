package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.ApplicantUtils

object V338 extends EditCheck[LoanApplicationRegister] with ApplicantUtils {
  override def name: String = "V338"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(notANaturalPerson(lar)) {
      lar.applicant.income is "NA"
    }
  }

  private def notANaturalPerson(lar: LoanApplicationRegister): Result = {
    val okAction = List(1, 2, 3, 4, 5, 7, 8)

    applicantNotNaturalPerson(lar.applicant) and
      coApplicantDoesNotExist(lar.applicant) and
      (lar.actionTakenType is containedIn(okAction))
  }
}
