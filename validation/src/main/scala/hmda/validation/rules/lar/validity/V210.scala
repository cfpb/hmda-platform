package hmda.validation.rules.lar.validity

import java.text.SimpleDateFormat

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.util.Try

object V210 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val date = lar.loan.applicationDate
    val format = new SimpleDateFormat("yyyyMMdd")
    format.setLenient(false)

    Try(format.parse(date).after(format.parse("20000101")) is equalTo(true))
      .getOrElse(date is equalTo("NA"))
  }

  override def name = "V210"
}
