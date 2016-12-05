package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Applicant, LoanApplicationRegister }
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V480 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V480"

  override def description = ""

  override def apply(lar: LoanApplicationRegister): Result = {
    val appl: Applicant = lar.applicant
    val raceFields: List[String] = List(appl.race1.toString, appl.race2, appl.race3, appl.race4, appl.race5)
    val races = raceFields.filterNot(_.isEmpty)

    races.distinct.size is equalTo(races.size)
  }
}
