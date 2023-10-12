package hmda.validation.rules.lar.validity_2018

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity._2018.V644_2

class V644_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V644_2

  property("If sex observed is true, sex must be male or female") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        applicant = lar.applicant.copy(
          sex = lar.applicant.sex.copy(sexEnum = MaleAndFemale)))

      val unapplicableLar = lar.copy(
        applicant =
          lar.applicant.copy(sex = lar.applicant.sex.copy(sexEnum = Male)))
      unapplicableLar.mustPass

      val sexNotVisual = applicableLar.applicant.sex
        .copy(sexObservedEnum = NotVisualOrSurnameSex)
      val sexNotApp = applicableLar.applicant.sex
        .copy(sexObservedEnum = SexObservedNotApplicable)
      val sexNC = applicableLar.applicant.sex
        .copy(sexObservedEnum = SexObservedNoCoApplicant)
      lar
        .copy(applicant = applicableLar.applicant.copy(sex = sexNotVisual))
        .mustPass
      lar
        .copy(applicant = applicableLar.applicant.copy(sex = sexNotApp))
        .mustFail
      lar
        .copy(applicant = applicableLar.applicant.copy(sex = sexNC))
        .mustFail
    }
  }
}
