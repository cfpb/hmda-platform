package hmda.validation.rules.lar.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionType._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

class V110(institution: Institution) extends EditCheck[TransmittalSheet] {
  override def name: String = "V110"

  override def apply(ts: TransmittalSheet): Result = {
    val parent = ts.parent

    when(institution.institutionType is oneOf(DependentMortgageCompany, Affiliate)) {
      (parent.name not empty) and
        (parent.address not empty) and
        (parent.city not empty) and
        (parent.state not empty) and
        (parent.zipCode not empty)
    }
  }
}
