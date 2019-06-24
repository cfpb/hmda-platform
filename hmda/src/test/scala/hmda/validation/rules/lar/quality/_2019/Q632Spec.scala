package hmda.validation.rules.lar.quality._2019

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q632Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q632

  property(
    "An invalid Automated Underwriting System data field was reported. Please review the information below and update your file, if needed:") {
    forAll(larGen) { lar =>
      lar
        .copy(
          AUS = lar.AUS.copy(aus1 = EmptyAUSValue,
                             aus2 = EmptyAUSValue,
                             aus3 = EmptyAUSValue,
                             aus4 = EmptyAUSValue,
                             aus5 = EmptyAUSValue))
        .mustPass

      val appLar = lar.copy(
        AUS = lar.AUS.copy(aus1 = EmptyAUSValue,
                           aus2 = TechnologyOpenToApprovedLenders,
                           aus3 = EmptyAUSValue,
                           aus4 = TechnologyOpenToApprovedLenders,
                           aus5 = EmptyAUSValue))

      appLar
        .copy(
          ausResult = appLar.ausResult.copy(
            ausResult1 = EmptyAUSResultValue,
            ausResult2 = EmptyAUSResultValue,
            ausResult3 = EmptyAUSResultValue,
            ausResult4 = EmptyAUSResultValue,
            ausResult5 = EmptyAUSResultValue
          ))
        .mustFail

      appLar
        .copy(
          ausResult = appLar.ausResult.copy(ausResult1 =
                                              OtherAutomatedUnderwritingResult,
                                            ausResult2 = Accept,
                                            ausResult3 = EmptyAUSResultValue,
                                            ausResult4 = Refer,
                                            ausResult5 = AcceptEligible))
        .mustPass
    }
  }
}
