package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q005Spec extends LarEditCheckSpec {
  property("All date formats must be either NA or in the format 'yyyyMMdd'") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q005
}
