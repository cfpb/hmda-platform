package hmda.validation.rules.lar.validity._2024

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V720_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V720-3"

  override def parent: String = "V720"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val scoringModels = List(
      FICOScore10,
      FICOScore10T,
      VantageScore4)

    when (lar.applicant.creditScoreType is containedIn(scoringModels)){
      lar.applicant.creditScore is greaterThanOrEqual(300)
    }
  }

}
