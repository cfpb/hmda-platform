package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.{ EditCheck, IfYearPresentIn }
import scala.util.Try

object Q022 {
  def inContext(context: ValidationContext): EditCheck[LoanApplicationRegister] = {
    IfYearPresentIn(context) { new Q022(_) }
  }
}

class Q022 private (year: Int) extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    // This edit should not fail if applicationDate is equal to "NA" or is otherwise non-convertible
    val applicationYear = parseYear(lar).getOrElse(year)

    // The two year time frame is to allow for construction loans, where properties are typically built within two years.
    // SEE: https://github.com/cfpb/hmda-platform/issues/469
    (year - applicationYear) is between(0, 2)
  }

  private def parseYear(lar: LoanApplicationRegister): Try[Int] = {
    Try(lar.loan.applicationDate.substring(0, 4).toInt)
  }

  def name = "Q022"
}
