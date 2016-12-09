package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.model.fi.lar.{ Applicant, LoanApplicationRegister }
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V495 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V495"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

  override def apply(lar: LoanApplicationRegister): Result = {
    val appl: Applicant = lar.applicant
    val coRaceFields: List[String] = List(appl.coRace1.toString, appl.coRace2, appl.coRace3, appl.coRace4, appl.coRace5)
    val coRaces = coRaceFields.filterNot(_.isEmpty)

    coRaces.distinct.size is equalTo(coRaces.size)
  }
}
