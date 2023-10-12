package hmda.validation.rules.lar.validity_2018

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.{Accept, ApproveEligible, ApproveIneligible, Caution, Eligible, EmptyAUSResultValue, Error, Incomplete, Ineligible, Invalid, OtherAUS, OtherAutomatedUnderwritingResult, OutOfScope, Refer, ReferEligilbe, ReferIneligible, ReferWithCaution, UnableToDetermineOrUnknown}
import hmda.validation.rules.lar.validity._2018.V699

class V699Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V699

  property("AUS Results must be valid if AUS is Other AUS") {
      val validAUSResults = List(
        ApproveEligible,
        ApproveIneligible,
        ReferEligilbe,
        ReferIneligible,
        ReferWithCaution,
        OutOfScope,
        Error,
        Accept,
        Caution,
        Ineligible,
        Incomplete,
        Invalid,
        Refer,
        Eligible,
        UnableToDetermineOrUnknown,
        OtherAutomatedUnderwritingResult
      )
      forAll(larGen) { lar =>
        lar.copy(AUS = lar.AUS.copy(aus1 = OtherAUS))
        whenever(
          (lar.AUS.aus1 != OtherAUS) &&
            (lar.AUS.aus2 != OtherAUS) &&
            (lar.AUS.aus3 != OtherAUS) &&
            (lar.AUS.aus4 != OtherAUS) &&
            (lar.AUS.aus5 != OtherAUS)
        ) {
          lar.mustPass
        }
      }
    forAll(larGen) { lar =>
      whenever(
        (validAUSResults contains lar.ausResult.ausResult1) &&
          (validAUSResults contains lar.ausResult.ausResult2) &&
          (validAUSResults contains lar.ausResult.ausResult3) &&
          (validAUSResults contains lar.ausResult.ausResult4) &&
          (validAUSResults contains lar.ausResult.ausResult5)
      ) {
        lar.mustPass
      }}
    forAll(larGen) { lar =>
      val relevantAUSLar1 = lar.copy(AUS = lar.AUS.copy(aus1 = OtherAUS))
      val relevantAUSLar2 = lar.copy(AUS = lar.AUS.copy(aus2 = OtherAUS))
      val relevantAUSLar3 = lar.copy(AUS = lar.AUS.copy(aus3 = OtherAUS))
      val relevantAUSLar4 = lar.copy(AUS = lar.AUS.copy(aus4 = OtherAUS))
      val relevantAUSLar5 = lar.copy(AUS = lar.AUS.copy(aus5 = OtherAUS))

      relevantAUSLar1
        .copy(
          ausResult =
            relevantAUSLar1.ausResult.copy(ausResult1 = EmptyAUSResultValue))
        .mustFail
      relevantAUSLar2
        .copy(
          ausResult =
            relevantAUSLar2.ausResult.copy(ausResult2 = EmptyAUSResultValue))
        .mustFail
      relevantAUSLar3
        .copy(
          ausResult =
            relevantAUSLar3.ausResult.copy(ausResult3 = EmptyAUSResultValue))
        .mustFail
      relevantAUSLar4
        .copy(
          ausResult =
            relevantAUSLar4.ausResult.copy(ausResult4 = EmptyAUSResultValue))
        .mustFail
      relevantAUSLar5
        .copy(
          ausResult =
            relevantAUSLar5.ausResult.copy(ausResult5 = EmptyAUSResultValue))
        .mustFail

    }
  }
}
