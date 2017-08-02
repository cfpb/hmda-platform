package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import scala.util.Try
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V485 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    val applicant = lar.applicant
    when(applicant.coRace1 is containedIn(1 to 5)) {
      (validRace(applicant.coRace2) is equalTo(true)) and
        (validRace(applicant.coRace3) is equalTo(true)) and
        (validRace(applicant.coRace4) is equalTo(true)) and
        (validRace(applicant.coRace5) is equalTo(true))
    }
  }

  private def validRace(input: String): Boolean = {
    (input == "") || inRange(1 to 5, input).getOrElse(false)
  }

  private def inRange(domain: Seq[Int], input: String): Try[Boolean] = {
    Try(domain.contains(input.toInt))
  }

  override def name: String = "V485"

}
