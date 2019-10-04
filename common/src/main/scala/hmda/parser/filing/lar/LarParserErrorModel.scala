package hmda.parser.filing.lar

import hmda.parser.ParserErrorModel._
import hmda.model.filing.lar.enums._

object LarParserErrorModel {

  case class InvalidId(value: String) extends ParserValidationError {
    override def fieldName: String = "Record Identifier"
    override def inputValue: String = value
    override def validValues: String = "2"
  }

  case class InvalidApplicationDate(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Application Date"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidLoanType(value: String) extends ParserValidationError {
    override def fieldName: String = "Loan Purpose"
    override def inputValue: String = value
    override def validValues: String = LoanTypeEnum.values.toString
  }

  case class InvalidLoanPurpose(value: String) extends ParserValidationError {
    override def fieldName: String = "Loan Purpose"
    override def inputValue: String = value
    override def validValues: String = LoanPurposeEnum.values.toString
  }

  case class InvalidPreapproval(value: String) extends ParserValidationError {
    override def fieldName: String = "Preapproval"
    override def inputValue: String = value
    override def validValues: String = PreapprovalEnum.values.toString
  }

  case class InvalidConstructionMethod(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Construction Method"
    override def inputValue: String = value
    override def validValues: String =
      ConstructionMethodEnum.values.toString
  }

  case class InvalidOccupancy(value: String) extends ParserValidationError {
    override def fieldName: String = "Occupancy Type"
    override def inputValue: String = value
    override def validValues: String = OccupancyEnum.values.toString
  }

  case class InvalidActionTaken(value: String) extends ParserValidationError {
    override def fieldName: String = "Action Taken"
    override def inputValue: String = value
    override def validValues: String = ActionTakenTypeEnum.values.toString
  }

  case class InvalidActionTakenDate(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Action Taken Date"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidAmount(value: String) extends ParserValidationError {
    override def fieldName: String = "Loan Amount"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidLoanTerm(value: String) extends ParserValidationError {
    override def fieldName: String = "Loan Term"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidApplicantEthnicity(fieldNumber: Int, value: String)
      extends ParserValidationError {
    override def fieldName: String =
      s"Ethnicity of Applicant or Borrower: ${fieldNumber.toString}"
    override def inputValue: String = value
    override def validValues: String = EthnicityEnum.values.toString
  }

  case class InvalidApplicantEthnicityObserved(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Ethnicity of Applicant or Borrower Collected on the Basis of Visual Observation or Surname"
    override def inputValue: String = value
    override def validValues: String = EthnicityObservedEnum.values.toString
  }

  case class InvalidCoApplicantEthnicity(fieldNumber: Int, value: String)
      extends ParserValidationError {
    override def fieldName: String =
      s"Ethnicity of Co-Applicant or Co-Borrower: ${fieldNumber.toString}"
    override def inputValue: String = value
    override def validValues: String = EthnicityEnum.values.toString
  }

  case class InvalidCoApplicantEthnicityObserved(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Ethnicity of Co-Applicant or CoBorrower Collected on the Basis of Visual Observation or Surname"
    override def inputValue: String = value
    override def validValues: String = EthnicityObservedEnum.values.toString
  }

  case class InvalidApplicantRace(fieldNumber: Int, value: String)
      extends ParserValidationError {
    override def fieldName: String =
      s"Race of Applicant or Borrower: ${fieldNumber.toString}"
    override def inputValue: String = value
    override def validValues: String = RaceEnum.values.toString
  }

  case class InvalidApplicantRaceObserved(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Race of Applicant or Borrower Collected on the Basis of Visual Observation or Surname"
    override def inputValue: String = value
    override def validValues: String = RaceObservedEnum.values.toString
  }

  case class InvalidCoApplicantRace(fieldNumber: Int, value: String)
      extends ParserValidationError {
    override def fieldName: String =
      s"Race of Co-Applicant or Co-Borrower: ${fieldNumber.toString}"
    override def inputValue: String = value
    override def validValues: String = RaceEnum.values.toString
  }

  case class InvalidCoApplicantRaceObserved(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Race of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname"
    override def inputValue: String = value
    override def validValues: String = RaceObservedEnum.values.toString
  }

  case class InvalidApplicantSex(value: String) extends ParserValidationError {
    override def fieldName: String = "Sex of Applicant or Borrower"
    override def inputValue: String = value
    override def validValues: String = SexEnum.values.toString
  }

  case class InvalidApplicantSexObserved(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Sex of Applicant or Borrower Collected on the Basis of Visual Observation or Surname"
    override def inputValue: String = value
    override def validValues: String = SexObservedEnum.values.toString
  }

  case class InvalidCoApplicantSex(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Sex of Co-Applicant or Co-Borrower"
    override def inputValue: String = value
    override def validValues: String = SexEnum.values.toString
  }

  case class InvalidCoApplicantSexObserved(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Sex of Co-Applicant or Co-Borrower Collected on the Basis of Visual Observation or Surname"
    override def inputValue: String = value
    override def validValues: String = SexObservedEnum.values.toString
  }

  case class InvalidApplicantAge(value: String) extends ParserValidationError {
    override def fieldName: String = "Age of Applicant or Borrower"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidCoApplicantAge(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Age of Co-Applicant or Co-Borrower"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidApplicantCreditScore(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Credit Score of Applicant or Borrower"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidApplicantCreditScoreModel(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Applicant or Borrower, Name and Version of Credit Scoring Model"
    override def inputValue: String = value
    override def validValues: String = CreditScoreEnum.values.toString
  }

  case class InvalidCoApplicantCreditScore(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Credit Score of Co-Applicant or Co-Borrower"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidCoApplicantCreditScoreModel(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Co-Applicant or Co-Borrower, Name and Version of Credit Scoring Model"
    override def inputValue: String = value
    override def validValues: String = CreditScoreEnum.values.toString
  }

  case class InvalidIncome(value: String) extends ParserValidationError {
    override def fieldName: String = "Income"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidPurchaserType(value: String) extends ParserValidationError {
    override def fieldName: String = "Type of Purchaser"
    override def inputValue: String = value
    override def validValues: String = PurchaserEnum.values.toString
  }

  case class InvalidRateSpread(value: String) extends ParserValidationError {
    override def fieldName: String = "Rate Spread"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidHoepaStatus(value: String) extends ParserValidationError {
    override def fieldName: String = "HOEPA Status"
    override def inputValue: String = value
    override def validValues: String = HOEPAStatusEnum.values.toString
  }

  case class InvalidLienStatus(value: String) extends ParserValidationError {
    override def fieldName: String = "Lien Status"
    override def inputValue: String = value
    override def validValues: String = LienStatusEnum.values.toString
  }

  case class InvalidDenial(fieldNumber: Int, value: String)
      extends ParserValidationError {
    override def fieldName: String =
      s"Reason for Denial: ${fieldNumber.toString}"
    override def inputValue: String = value
    override def validValues: String = DenialReasonEnum.values.toString
  }

  case class InvalidTotalLoanCosts(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Total Loan Costs"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidPointsAndFees(value: String) extends ParserValidationError {
    override def fieldName: String = "Total Points and Fees"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidOriginationCharges(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Origination Charges"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidDiscountPoints(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Discount Points"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidLenderCredits(value: String) extends ParserValidationError {
    override def fieldName: String = "Lender Credits"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidInterestRate(value: String) extends ParserValidationError {
    override def fieldName: String = "Interest Rate"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidPrepaymentPenaltyTerm(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Prepayment Penalty Term"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidDebtToIncomeRatio(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Debt-to-Income Ratio"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidLoanToValueRatio(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Combined Loan-to-Value Ratio"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidIntroductoryRatePeriod(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Introductory Rate Period"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidBalloonPayment(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Balloon Payments"
    override def inputValue: String = value
    override def validValues: String = BalloonPaymentEnum.values.toString
  }

  case class InvalidInterestOnlyPayment(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Interest-Only Payments"
    override def inputValue: String = value
    override def validValues: String =
      InterestOnlyPaymentsEnum.values.toString
  }

  case class InvalidNegativeAmortization(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Negative Amortization"
    override def inputValue: String = value
    override def validValues: String = NegativeAmortizationEnum.values.toString
  }

  case class InvalidOtherNonAmortizingFeatures(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Other Non-Amortizing Features"
    override def inputValue: String = value
    override def validValues: String =
      OtherNonAmortizingFeaturesEnum.values.toString
  }

  case class InvalidPropertyValue(value: String) extends ParserValidationError {
    override def fieldName: String = "Property Value"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidManufacturedHomeSecuredProperty(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Manufactured Home Secured Property Type"
    override def inputValue: String = value
    override def validValues: String =
      ManufacturedHomeSecuredPropertyEnum.values.toString
  }

  case class InvalidManufacturedHomeLandPropertyInterest(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Manufactured Home Land Property Interest"
    override def inputValue: String = value
    override def validValues: String =
      ManufacturedHomeLandPropertyInterestEnum.values.toString
  }

  case class InvalidTotalUnits(value: String) extends ParserValidationError {
    override def fieldName: String = "Total Units"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidMultifamilyUnits(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Multifamily Affordable Units"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidApplicationSubmission(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Submission of Application"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidPayableToInstitution(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Initially Payable to Your Institution"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidNMLSRIdentifier(value: String)
      extends ParserValidationError {
    override def fieldName: String =
      "Mortgage Loan Originiator NMLSR Identifier"
    override def inputValue: String = value
    override def validValues: String = ""
  }

  case class InvalidAutomatedUnderwritingSystem(fieldNumber: Int, value: String)
      extends ParserValidationError {
    override def fieldName: String =
      s"Automated Underwriting System: ${fieldNumber.toString}"
    override def inputValue: String = value
    override def validValues: String =
      AutomatedUnderwritingSystemEnum.values.toString
  }

  case class InvalidAutomatedUnderwritingSystemResult(fieldNumber: Int,
                                                      value: String)
      extends ParserValidationError {
    override def fieldName: String =
      s"Automated Underwriting System Result: ${fieldNumber.toString}"
    override def inputValue: String = value
    override def validValues: String =
      AutomatedUnderwritingResultEnum.values.toString
  }

  case class InvalidMortgageType(value: String) extends ParserValidationError {
    override def fieldName: String = "Reverse Mortage"
    override def inputValue: String = value
    override def validValues: String = MortgageTypeEnum.values.toString
  }

  case class InvalidLineOfCredit(value: String) extends ParserValidationError {
    override def fieldName: String = "Open-End Line of Credit"
    override def inputValue: String = value
    override def validValues: String = LineOfCreditEnum.values.toString
  }

  case class InvalidBusinessOrCommercial(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Buisness or Commercial Purpose"
    override def inputValue: String = value
    override def validValues: String =
      BusinessOrCommercialBusinessEnum.values.toString
  }

}
