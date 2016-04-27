package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V470 extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    val applicant = lar.applicant
    when(applicant.race1 is containedIn(1 to 5)) {
      (validRace(applicant.race2) is equalTo(true)) and
        (validRace(applicant.race3) is equalTo(true)) and
        (validRace(applicant.race4) is equalTo(true)) and
        (validRace(applicant.race5) is equalTo(true))
    }
  }

  private def validRace(input: String): Boolean = {
    (input == "") || inRange(1 to 5, input)
  }

  private def inRange(domain: Seq[Int], input: String): Boolean = {
    try {
      val x = input.toInt
      domain.contains(x)
    } catch {
      case ex: NumberFormatException => false
    }
  }

  def name: String = "V470"

}
