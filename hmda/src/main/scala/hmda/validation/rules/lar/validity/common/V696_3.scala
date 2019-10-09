package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V696_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V696-3"

  override def parent: String = "V696"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val listAus =
      List(lar.AUS.aus1, lar.AUS.aus2, lar.AUS.aus3, lar.AUS.aus4, lar.AUS.aus5)
    val listRes =
      List(lar.ausResult.ausResult1, lar.ausResult.ausResult2, lar.ausResult.ausResult3, lar.ausResult.ausResult4, lar.ausResult.ausResult5)

    val sizeAus = listAus.filterNot(_ == EmptyAUSValue).size
    val sizeRes = listRes.filterNot(_ == EmptyAUSResultValue).size

    sizeAus is equalTo(sizeRes)
  }
}
