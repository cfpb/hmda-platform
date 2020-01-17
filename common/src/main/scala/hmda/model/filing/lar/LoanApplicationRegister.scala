package hmda.model.filing.lar

import hmda.model.filing.lar.enums._
import hmda.model.filing.{HmdaFileRow, PipeDelimited}
import io.circe._
import io.circe.syntax._

case class LoanApplicationRegister(
                                    larIdentifier: LarIdentifier = LarIdentifier(),
                                    loan: Loan = Loan(),
                                    action: LarAction = LarAction(),
                                    geography: Geography = Geography(),
                                    applicant: Applicant = Applicant(),
                                    coApplicant: Applicant = Applicant(),
                                    income: String = "",
                                    purchaserType: PurchaserEnum = new InvalidPurchaserCode,
                                    hoepaStatus: HOEPAStatusEnum = new InvalidHoepaStatusCode,
                                    lienStatus: LienStatusEnum = new InvalidLienStatusCode,
                                    denial: Denial = Denial(),
                                    loanDisclosure: LoanDisclosure = LoanDisclosure(),
                                    nonAmortizingFeatures: NonAmortizingFeatures = NonAmortizingFeatures(),
                                    property: Property = Property(),
                                    applicationSubmission: ApplicationSubmissionEnum =
                                    new InvalidApplicationSubmissionCode,
                                    payableToInstitution: PayableToInstitutionEnum =
                                    new InvalidPayableToInstitutionCode,
                                    AUS: AutomatedUnderwritingSystem = AutomatedUnderwritingSystem(),
                                    ausResult: AutomatedUnderwritingSystemResult =
                                    AutomatedUnderwritingSystemResult(),
                                    reverseMortgage: MortgageTypeEnum = new InvalidMortgageTypeCode,
                                    lineOfCredit: LineOfCreditEnum = new InvalidLineOfCreditCode,
                                    businessOrCommercialPurpose: BusinessOrCommercialBusinessEnum =
                                    new InvalidBusinessOrCommercialBusinessCode
                                  ) extends PipeDelimited
  with HmdaFileRow {

  override def toCSV: String = {

    s"${larIdentifier.id}|${larIdentifier.LEI}|${loan.ULI}|${loan.applicationDate}|${loan.loanType.code}|${loan.loanPurpose.code}|${action.preapproval.code}|" +
      s"${loan.constructionMethod.code}|${loan.occupancy.code}|${loan.amount}|${action.actionTakenType.code}|${action.actionTakenDate}|" +
      s"${geography.toCSV}|" +
      s"${applicant.ethnicity.ethnicity1.code}|${applicant.ethnicity.ethnicity2.code}|${applicant.ethnicity.ethnicity3.code}|" +
      s"${applicant.ethnicity.ethnicity4.code}|${applicant.ethnicity.ethnicity5.code}|${applicant.ethnicity.otherHispanicOrLatino}|" +
      s"${coApplicant.ethnicity.ethnicity1.code}|${coApplicant.ethnicity.ethnicity2.code}|${coApplicant.ethnicity.ethnicity3.code}|" +
      s"${coApplicant.ethnicity.ethnicity4.code}|${coApplicant.ethnicity.ethnicity5.code}|${coApplicant.ethnicity.otherHispanicOrLatino}|" +
      s"${applicant.ethnicity.ethnicityObserved.code}|${coApplicant.ethnicity.ethnicityObserved.code}|${applicant.race.race1.code}|" +
      s"${applicant.race.race2.code}|${applicant.race.race3.code}|${applicant.race.race4.code}|${applicant.race.race5.code}|${applicant.race.otherNativeRace}|" +
      s"${applicant.race.otherAsianRace}|${applicant.race.otherPacificIslanderRace}|${coApplicant.race.race1.code}|${coApplicant.race.race2.code}|" +
      s"${coApplicant.race.race3.code}|${coApplicant.race.race4.code}|${coApplicant.race.race5.code}|${coApplicant.race.otherNativeRace}|${coApplicant.race.otherAsianRace}|" +
      s"${coApplicant.race.otherPacificIslanderRace}|${applicant.race.raceObserved.code}|${coApplicant.race.raceObserved.code}|" +
      s"${applicant.sex.sexEnum.code}|${coApplicant.sex.sexEnum.code}|${applicant.sex.sexObservedEnum.code}|${coApplicant.sex.sexObservedEnum.code}|" +
      s"${applicant.age}|${coApplicant.age}|$income|${purchaserType.code}|${loan.rateSpread}|${hoepaStatus.code}|${lienStatus.code}|${applicant.creditScore}|${coApplicant.creditScore}|" +
      s"${applicant.creditScoreType.code}|${applicant.otherCreditScoreModel}|${coApplicant.creditScoreType.code}|${coApplicant.otherCreditScoreModel}|" +
      s"${denial.denialReason1.code}|${denial.denialReason2.code}|${denial.denialReason3.code}|${denial.denialReason4.code}|${denial.otherDenialReason}|${loanDisclosure.totalLoanCosts}|" +
      s"${loanDisclosure.totalPointsAndFees}|${loanDisclosure.originationCharges}|${loanDisclosure.discountPoints}|${loanDisclosure.lenderCredits}|${loan.interestRate}|" +
      s"${loan.prepaymentPenaltyTerm}|${loan.debtToIncomeRatio}|${loan.combinedLoanToValueRatio}|${loan.loanTerm}|${loan.introductoryRatePeriod}|${nonAmortizingFeatures.balloonPayment.code}|" +
      s"${nonAmortizingFeatures.interestOnlyPayments.code}|${nonAmortizingFeatures.negativeAmortization.code}|${nonAmortizingFeatures.otherNonAmortizingFeatures.code}|" +
      s"${property.propertyValue}|${property.manufacturedHomeSecuredProperty.code}|${property.manufacturedHomeLandPropertyInterest.code}|${property.totalUnits}|${property.multiFamilyAffordableUnits}|${applicationSubmission.code}|" +
      s"${payableToInstitution.code}|${larIdentifier.NMLSRIdentifier}|${AUS.toCSV}|${ausResult.toCSV}|${reverseMortgage.code}|${lineOfCredit.code}|${businessOrCommercialPurpose.code}"

  }

  override def valueOf(field: String): String = {
    LarFieldMapping
      .mapping(this)
      .getOrElse(field, s"error: field name mismatch for $field")
  }
}

object LoanApplicationRegister {
  implicit val larEncoder: Encoder[LoanApplicationRegister] =
    (a: LoanApplicationRegister) =>
      Json.obj(
        ("larIdentifier", a.larIdentifier.asJson),
        ("loan", a.loan.asJson),
        ("larAction", a.action.asJson),
        ("geography", a.geography.asJson),
        ("applicant", a.applicant.asJson),
        ("coApplicant", a.coApplicant.asJson),
        ("income", Json.fromString(a.income)),
        ("purchaserType", a.purchaserType.asInstanceOf[LarEnum].asJson),
        ("hoepaStatus", a.hoepaStatus.asInstanceOf[LarEnum].asJson),
        ("lienStatus", a.lienStatus.asInstanceOf[LarEnum].asJson),
        ("denial", a.denial.asJson),
        ("loanDisclosure", a.loanDisclosure.asJson),
        ("nonAmortizingFeatures", a.nonAmortizingFeatures.asJson),
        ("property", a.property.asJson),
        ("applicationSubmission",
          a.applicationSubmission.asInstanceOf[LarEnum].asJson),
        ("payableToInstitution",
          a.payableToInstitution.asInstanceOf[LarEnum].asJson),
        ("AUS", a.AUS.asJson),
        ("ausResult", a.ausResult.asJson),
        ("reverseMortgage", a.reverseMortgage.asInstanceOf[LarEnum].asJson),
        ("lineOfCredit", a.lineOfCredit.asInstanceOf[LarEnum].asJson),
        ("businessOrCommercialPurpose",
          a.businessOrCommercialPurpose.asInstanceOf[LarEnum].asJson)
      )

  implicit val larDecoder: Decoder[LoanApplicationRegister] =
    (c: HCursor) =>
      for {
        larIdentifier <- c.downField("larIdentifier").as[LarIdentifier]
        loan <- c.downField("loan").as[Loan]
        larAction <- c.downField("larAction").as[LarAction]
        geography <- c.downField("geography").as[Geography]
        applicant <- c.downField("applicant").as[Applicant]
        coApplicant <- c.downField("coApplicant").as[Applicant]
        income <- c.downField("income").as[String]
        purchaserType <- c.downField("purchaserType").as[Int]
        hoepaStatus <- c.downField("hoepaStatus").as[Int]
        lienStatus <- c.downField("lienStatus").as[Int]
        denial <- c.downField("denial").as[Denial]
        loanDisclosure <- c.downField("loanDisclosure").as[LoanDisclosure]
        nonAmortizingFeatures <- c
          .downField("nonAmortizingFeatures")
          .as[NonAmortizingFeatures]
        property <- c.downField("property").as[Property]
        applicationSubmission <- c.downField("applicationSubmission").as[Int]
        payableToInstitution <- c.downField("payableToInstitution").as[Int]
        aus <- c.downField("AUS").as[AutomatedUnderwritingSystem]
        ausResult <- c
          .downField("ausResult")
          .as[AutomatedUnderwritingSystemResult]
        reverseMortgage <- c.downField("reverseMortgage").as[Int]
        lineOfCredit <- c.downField("lineOfCredit").as[Int]
        businessOrCommercialPurpose <- c
          .downField("businessOrCommercialPurpose")
          .as[Int]
      } yield
        LoanApplicationRegister(
          larIdentifier,
          loan,
          larAction,
          geography,
          applicant,
          coApplicant,
          income,
          PurchaserEnum.valueOf(purchaserType),
          HOEPAStatusEnum.valueOf(hoepaStatus),
          LienStatusEnum.valueOf(lienStatus),
          denial,
          loanDisclosure,
          nonAmortizingFeatures,
          property,
          ApplicationSubmissionEnum.valueOf(applicationSubmission),
          PayableToInstitutionEnum.valueOf(payableToInstitution),
          aus,
          ausResult,
          MortgageTypeEnum.valueOf(reverseMortgage),
          LineOfCreditEnum.valueOf(lineOfCredit),
          BusinessOrCommercialBusinessEnum.valueOf(businessOrCommercialPurpose)
        )
}