package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V628_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V628_3

  property("Ethnicity codes cannot be repeated") {
    forAll(larGen) { lar =>
      val validEthnicity1 = lar.applicant.ethnicity.copy(
        ethnicity1 = EmptyEthnicityValue,
        ethnicity2 = EmptyEthnicityValue,
        ethnicity3 = EmptyEthnicityValue,
        ethnicity4 = EmptyEthnicityValue,
        ethnicity5 = EmptyEthnicityValue
      )

      val validEthnicity2 =
        lar.applicant.ethnicity.copy(ethnicity1 = HispanicOrLatino,
                                     ethnicity2 = Mexican,
                                     ethnicity3 = PuertoRican,
                                     ethnicity4 = Cuban,
                                     ethnicity5 = OtherHispanicOrLatino)

      val invalidEthnicity =
        lar.applicant.ethnicity.copy(ethnicity2 = EmptyEthnicityValue,
                                     ethnicity3 = HispanicOrLatino,
                                     ethnicity4 = HispanicOrLatino,
                                     ethnicity5 = EmptyEthnicityValue)

      val validLar1 =
        lar.copy(applicant = lar.applicant.copy(ethnicity = validEthnicity1))
      validLar1.mustPass

      val validLar2 =
        lar.copy(applicant = lar.applicant.copy(ethnicity = validEthnicity2))
      validLar2.mustPass

      val invalidLar =
        lar.copy(applicant = lar.applicant.copy(ethnicity = invalidEthnicity))
      invalidLar.mustFail

    }
  }
}
