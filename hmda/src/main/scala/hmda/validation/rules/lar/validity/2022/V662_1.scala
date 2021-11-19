package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V662_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V662-1"

  override def parent: String = "V662"

  val creditList = List(
    CreditScoreExempt,
    EquifaxBeacon5,
    ExperianFairIsaac,
    FICORiskScoreClassic04,
    FICORiskScoreClassic98,
    VantageScore2,
    VantageScore3,
    OneOrMoreCreditScoreModels,
    CreditScoreNotApplicable,
    FICOScore9
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.creditScoreType is containedIn(creditList)) {
      lar.applicant.otherCreditScoreModel is empty
    } and when(lar.applicant.otherCreditScoreModel is empty) {
      lar.applicant.creditScoreType is containedIn(creditList)
    }
}
