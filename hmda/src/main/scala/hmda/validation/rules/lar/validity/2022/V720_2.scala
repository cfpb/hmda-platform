package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V720_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V720-2"

  override def parent: String = "V720"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val scoringModels = List(1, 2, 3, 4, 5, 6, 11)

    when (lar.coApplicant.creditScoreType.code is containedIn(scoringModels)) {
      lar.coApplicant.creditScore is greaterThanOrEqual(280)
    }
  }

}
