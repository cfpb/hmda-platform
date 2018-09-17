package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  ManufacturedHome,
  ManufacturedHomeLoanPropertyInterestExempt,
  SiteBuilt,
  UnpaidLeasehold
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V690_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V690_3

  property(
    "If construction method is site built, manufactured property interest must be exempt or NA") {
    forAll(larGen) { lar =>
      whenever(lar.loan.constructionMethod == ManufacturedHome) {
        lar.mustPass
      }

      val appLar =
        lar.copy(loan = lar.loan.copy(constructionMethod = SiteBuilt))
      appLar
        .copy(
          property = appLar.property.copy(
            manufacturedHomeLandPropertyInterest = UnpaidLeasehold))
        .mustFail
      appLar
        .copy(
          property = appLar.property.copy(manufacturedHomeLandPropertyInterest =
            ManufacturedHomeLoanPropertyInterestExempt))
        .mustPass
    }
  }
}
