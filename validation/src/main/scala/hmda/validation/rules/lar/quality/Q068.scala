package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.ApplicantUtils

object Q068 extends EditCheck[LoanApplicationRegister] with ApplicantUtils {
  override def name: String = "Q068"

  override def apply(lar: LoanApplicationRegister): Result = {
    val app = lar.applicant
    when(lar.actionTakenType is oneOf(1, 2, 3, 4, 5, 7, 8) and
      applicantNotNaturalPerson(app)) {
      app.coEthnicity not equalTo(4) and
        (app.coRace1 not equalTo(7)) and
        (app.coSex not equalTo(4))
    }
  }

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )
}
