package hmda.validation.rules.lar.validity._2024

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V665_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V665-2"

  override def parent: String = "V665"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val scoringModels = List(
      EquifaxBeacon5,
      ExperianFairIsaac,
      FICORiskScoreClassic04,
      FICORiskScoreClassic98,
      VantageScore2,
      VantageScore3,
      OneOrMoreCreditScoreModels,
      OtherCreditScoreModel,
      CreditScoreNotApplicable,
      CreditScoreNoCoApplicant,
      CreditScoreExempt,
      FICOScore9,
      FICOScore8,
      FICOScore10,
      FICOScore10T,
      VantageScore4
    )
    lar.coApplicant.creditScoreType is containedIn(scoringModels)
  }
}
