package hmda.validation.rules.lar.validity

import java.text.SimpleDateFormat

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Result, Success }
import hmda.validation.rules.EditCheck

object V265 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val date = lar.actionTakenDate.toString

    try {
      val format = new SimpleDateFormat("yyyyMMdd")
      format.setLenient(false)
      format.parse(date)
      Success()
    } catch {
      case e: Exception => Failure("Date not properly formatted")
    }
  }

  override def name = "V265"
}
