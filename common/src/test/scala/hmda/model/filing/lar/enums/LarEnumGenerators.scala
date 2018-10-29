package hmda.model.filing.lar.enums

import org.scalacheck.Gen

object LarEnumGenerators {

  implicit def preapprovalEnumGen: Gen[PreapprovalEnum] =
    enumGen(PreapprovalEnum)

  implicit def actionTakenTypeEnumGen: Gen[ActionTakenTypeEnum] =
    enumGen(ActionTakenTypeEnum)

  implicit def purchaserEnumGen: Gen[PurchaserEnum] = enumGen(PurchaserEnum)

  implicit def hOEPAStatusEnumGen: Gen[HOEPAStatusEnum] =
    enumGen(HOEPAStatusEnum)

  implicit def lienStatusEnumGen: Gen[LienStatusEnum] = enumGen(LienStatusEnum)

  implicit def ballonPaymentEnumGen: Gen[BalloonPaymentEnum] =
    enumGen(BalloonPaymentEnum)

  implicit def interestOnlyPayementsEnumGen: Gen[InterestOnlyPaymentsEnum] =
    enumGen(InterestOnlyPaymentsEnum)

  implicit def negativeAmortizationEnumGen: Gen[NegativeAmortizationEnum] =
    enumGen(NegativeAmortizationEnum)

  implicit def otherNonAmortizingFeaturesEnumGen
    : Gen[OtherNonAmortizingFeaturesEnum] =
    enumGen(OtherNonAmortizingFeaturesEnum)

  implicit def manufacturedHomeSecuredPropertyEnumGen
    : Gen[ManufacturedHomeSecuredPropertyEnum] =
    enumGen(ManufacturedHomeSecuredPropertyEnum)

  implicit def manufacturedHomeLandPropertyInterestEnumGen
    : Gen[ManufacturedHomeLandPropertyInterestEnum] =
    enumGen(ManufacturedHomeLandPropertyInterestEnum)

  implicit def applicationSubmissionEnumGen: Gen[ApplicationSubmissionEnum] =
    enumGen(ApplicationSubmissionEnum)

  implicit def payableToInstitutionEnumGen: Gen[PayableToInstitutionEnum] =
    enumGen(PayableToInstitutionEnum)

  implicit def mortgageTypeEnumGen: Gen[MortgageTypeEnum] =
    enumGen(MortgageTypeEnum)

  implicit def lineOfCreditEnumGen: Gen[LineOfCreditEnum] =
    enumGen(LineOfCreditEnum)

  implicit def businessOrCommercialBusinessEnumGen
    : Gen[BusinessOrCommercialBusinessEnum] =
    enumGen(BusinessOrCommercialBusinessEnum)

  implicit def loanTypeEnumGen: Gen[LoanTypeEnum] = enumGen(LoanTypeEnum)

  implicit def loanPurposeEnumGen: Gen[LoanPurposeEnum] =
    enumGen(LoanPurposeEnum)

  implicit def constructionMethodEnumGen: Gen[ConstructionMethodEnum] =
    enumGen(ConstructionMethodEnum)

  implicit def occupancyEnumGen: Gen[OccupancyEnum] = enumGen(OccupancyEnum)

  implicit def denialReasonEnumGen: Gen[DenialReasonEnum] =
    enumGen(DenialReasonEnum)

  implicit def ethnicityEnumGen: Gen[EthnicityEnum] = enumGen(EthnicityEnum)

  implicit def ethnicifyObserverdEnumGen: Gen[EthnicityObservedEnum] =
    enumGen(EthnicityObservedEnum)

  implicit def automatedUnderwritingSystemEnumGen
    : Gen[AutomatedUnderwritingSystemEnum] =
    enumGen(AutomatedUnderwritingSystemEnum)
  implicit def automatedUnderWritingSystemResultEnumGen
    : Gen[AutomatedUnderwritingResultEnum] =
    enumGen(AutomatedUnderwritingResultEnum)

  implicit def raceEnumGen: Gen[RaceEnum] = enumGen(RaceEnum)

  implicit def raceObservedEnumGen: Gen[RaceObservedEnum] =
    enumGen(RaceObservedEnum)

  implicit def sexEnumGen: Gen[SexEnum] = enumGen(SexEnum)

  implicit def sexObservedEnumGen: Gen[SexObservedEnum] =
    enumGen(SexObservedEnum)

  implicit def creditScoreEnumGen: Gen[CreditScoreEnum] =
    enumGen(CreditScoreEnum)

  implicit def mortgageTypeEnum: Gen[MortgageTypeEnum] =
    enumGen(MortgageTypeEnum)

  private def enumGen[A](larCodeEnum: LarCodeEnum[A]): Gen[A] = {
    Gen.oneOf(larCodeEnum.values).map(i => larCodeEnum.valueOf(i))
  }

}
