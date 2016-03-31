package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, CommonDsl, Result, Success }

object V375 extends CommonDsl {

  val okLoanTypes = List(2, 3, 4)

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.purchaserType is equalTo(2), lar.loan.loanType is containedIn(okLoanTypes))
  }

  // if we like this then let's move it up to CommonDsl. one question: are commas better, or a curried something? yum.
  // another question/idea: would we want to introduce a "then" concept to make it read better?
  def when(condition: Result, thenTest: Result): Result = {
    condition match {
      case Success() => thenTest
      case Failure(_) => Success()
    }
  }
}
