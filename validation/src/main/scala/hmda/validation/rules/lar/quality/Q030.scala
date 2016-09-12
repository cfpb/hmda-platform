package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateGeo._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ Failure, Result, Success }
import hmda.validation.rules.{ EditCheck, IfInstitutionPresentIn }

class Q030 private (institution: Institution) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q030"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is containedIn(1 to 6)) {
      val geo: Geography = lar.geography
      (geo, institution.cra) match {
        case (Geography("NA", "NA", "NA", "NA"), false) => Success()
        case (Geography(_, "NA", _, _), _) => Failure()
        case (Geography(_, _, "NA", _), _) => Failure()
        case _ =>
          when(institution.cra is false) {
            when(geo is smallCounty) {
              geo.tract is "NA"
            }
          } and when(geo.tract is "NA") {
            geo is smallCounty // and therefore also a valid state/county combination; no need to check again.
          } and when(geo.msa not "NA") {
            (geo is validStateCountyMsaCombination) and
              when(geo.tract not "NA") {
                geo is validCompleteCombination
              }
          } and when(geo.msa is "NA") {
            when(geo.tract not "NA") {
              geo is validStateCountyTractCombination
            }
          }
      }
    }
  }
}

object Q030 {
  def inContext(context: ValidationContext): EditCheck[LoanApplicationRegister] = {
    IfInstitutionPresentIn(context) { new Q030(_) }
  }
}
