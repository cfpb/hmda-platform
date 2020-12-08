package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.model.filing.lar.enums._

object V696_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V696-3"

  override def parent: String = "V696"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    when (lar.AUS.aus2 not equalTo(EmptyAUSValue)){lar.ausResult.ausResult2 not equalTo(EmptyAUSResultValue)} and
    when (lar.AUS.aus3 not equalTo(EmptyAUSValue)){lar.ausResult.ausResult3 not equalTo(EmptyAUSResultValue)} and
    when (lar.AUS.aus4 not equalTo(EmptyAUSValue)){lar.ausResult.ausResult4 not equalTo(EmptyAUSResultValue)} and
    when (lar.AUS.aus5 not equalTo(EmptyAUSValue)){lar.ausResult.ausResult5 not equalTo(EmptyAUSResultValue)} and
    when (lar.ausResult.ausResult2 not equalTo(EmptyAUSResultValue)){lar.AUS.aus2 not equalTo(EmptyAUSValue)} and
    when (lar.ausResult.ausResult3 not equalTo(EmptyAUSResultValue)){lar.AUS.aus3 not equalTo(EmptyAUSValue)} and
    when (lar.ausResult.ausResult4 not equalTo(EmptyAUSResultValue)){lar.AUS.aus4 not equalTo(EmptyAUSValue)} and
    when (lar.ausResult.ausResult5 not equalTo(EmptyAUSResultValue)){lar.AUS.aus5 not equalTo(EmptyAUSValue)} and
    when (lar.AUS.aus2 is equalTo(EmptyAUSValue)){lar.ausResult.ausResult2 is equalTo(EmptyAUSResultValue)} and
    when (lar.AUS.aus3 is equalTo(EmptyAUSValue)){lar.ausResult.ausResult3 is equalTo(EmptyAUSResultValue)} and
    when (lar.AUS.aus4 is equalTo(EmptyAUSValue)){lar.ausResult.ausResult4 is equalTo(EmptyAUSResultValue)} and
    when (lar.AUS.aus5 is equalTo(EmptyAUSValue)){lar.ausResult.ausResult5 is equalTo(EmptyAUSResultValue)} and
    when (lar.ausResult.ausResult2 is equalTo(EmptyAUSResultValue)){lar.AUS.aus2 is equalTo(EmptyAUSValue)} and
    when (lar.ausResult.ausResult3 is equalTo(EmptyAUSResultValue)){lar.AUS.aus3 is equalTo(EmptyAUSValue)} and
    when (lar.ausResult.ausResult4 is equalTo(EmptyAUSResultValue)){lar.AUS.aus4 is equalTo(EmptyAUSValue)} and
    when (lar.ausResult.ausResult5 is equalTo(EmptyAUSResultValue)){lar.AUS.aus5 is equalTo(EmptyAUSValue)}
    
  }

}
