package hmda.validation.rules.lar.validity

import java.text.SimpleDateFormat

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V210 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val date = lar.loan.applicationDate

    try {
      val format = new SimpleDateFormat("yyyyMMdd")
      format.setLenient(false)
      val dateObject = format.parse(date)
      dateObject.after(format.parse("20000101")) is equalTo(true)
    } catch {
      case e: Exception => date is equalTo("NA")
    }
  }

  override def name = "V210"
}
