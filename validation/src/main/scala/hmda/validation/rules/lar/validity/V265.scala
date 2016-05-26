package hmda.validation.rules.lar.validity

import java.text.SimpleDateFormat

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.util.Try

object V265 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val date = lar.actionTakenDate.toString

    val format = new SimpleDateFormat("yyyyMMdd")
    format.setLenient(false)

    Try(format.parse(date)).isSuccess is equalTo(true)
  }

  override def name = "V265"
}
