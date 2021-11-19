package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V721_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V721-2"

  override def parent: String = "V721"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val coAge = lar.coApplicant.age

    when (coAge is numeric){
      coAge not equalTo(1111)
    }
  }

}
