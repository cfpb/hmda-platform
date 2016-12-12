package hmda.validation.rules.lar.validity

import java.text.SimpleDateFormat

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.util.Try

object V265 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val date = lar.actionTakenDate.toString
    val dateFormat = "yyyyMMdd"

    val format = new SimpleDateFormat(dateFormat)
    format.setLenient(false)

    (Try(format.parse(date)).isSuccess is equalTo(true)) and
      (date.length is equalTo(dateFormat.length))
  }

  override def name = "V265"

}
