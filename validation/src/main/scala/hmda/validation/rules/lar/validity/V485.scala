package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Predicate, Result }
import hmda.validation.rules.EditCheck
import scala.util.Try

object V485 extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    val applicant = lar.applicant
    when(applicant.coRace1 is containedIn(1 to 5)) {
      (applicant.coRace2 is validRace) and
        (applicant.coRace3 is validRace) and
        (applicant.coRace4 is validRace) and
        (applicant.coRace5 is validRace)
    }
  }

  def validRace: Predicate[String] = new Predicate[String] {
    override def validate: String => Boolean = blankOr1to5(_)
    override def failure: String = "must be 1-5 or blank"
  }

  private def blankOr1to5(input: String): Boolean = {
    (input == "") || Try((1 to 5).contains(input.toInt)).getOrElse(false)
  }

  def name: String = "V485"

}
