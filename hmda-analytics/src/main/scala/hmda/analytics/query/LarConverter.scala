package hmda.analytics.query

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.derivedFields._
import hmda.model.census.CountyLoanLimit
import hmda.census.records._
import hmda.census.records.CountyLoanLimitRecords._
import hmda.model.census.Census
import com.typesafe.config.ConfigFactory

object LarConverter {

  val config = ConfigFactory.load()

  val censusFileName2019 =
    config.getString("hmda.census.fields.2019.filename")

  val censusTractMap: Map[String, Census] =
    CensusRecords.indexedTract2019

  val censusRecords = CensusRecords.parseCensusFile(censusFileName2019)

  val countyLoanLimitFileName2018 =
      config.getString("hmda.countyLoanLimit.2018.fields.filename")
  val countyLoanLimitFileName2019 =
      config.getString("hmda.countyLoanLimit.2019.fields.filename")
  val countyLoanLimitFileName2020 =
      config.getString("hmda.countyLoanLimit.2020.fields.filename")
  
  val countyLoanLimits2018: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2018)
  val countyLoanLimits2019: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2019)
  val countyLoanLimits2020: Seq[CountyLoanLimit] =
    parseCountyLoanLimitFile(countyLoanLimitFileName2020)

  val overallLoanLimit2018 = overallLoanLimits(countyLoanLimits2018)
  val overallLoanLimit2019 = overallLoanLimits(countyLoanLimits2019)
  val overallLoanLimit2020 = overallLoanLimits(countyLoanLimits2020)

  val countyLoanLimitsByCounty2018 = countyLoansLimitByCounty(countyLoanLimits2018)
  val countyLoanLimitsByCounty2019 = countyLoansLimitByCounty(countyLoanLimits2019)
  val countyLoanLimitsByCounty2020 = countyLoansLimitByCounty(countyLoanLimits2020)

  val countyLoanLimitsByState2018 = countyLoansLimitByState(countyLoanLimits2018)
  val countyLoanLimitsByState2019 = countyLoansLimitByState(countyLoanLimits2019)
  val countyLoanLimitsByState2020 = countyLoansLimitByState(countyLoanLimits2020)

  def apply(
    lar: LoanApplicationRegister,
    year: Int,
    isQuarterly: Boolean = false
  ): LarEntity = {
    val census = censusTractMap.getOrElse(lar.geography.tract, Census())
    val overallLoanLimit = getOverallLoanLimit(year)
    val countyLoanLimitsByCounty = getcountyLoanLimitsByCounty(year)
    val countyLoanLimitsByState = getcountyLoanLimitsByState(year)
    LarEntity(
      lar.larIdentifier.id,
      lar.larIdentifier.LEI,
      lar.loan.ULI,
      lar.loan.applicationDate,
      lar.loan.loanType.code,
      lar.loan.loanPurpose.code,
      lar.action.preapproval.code,
      lar.loan.constructionMethod.code,
      lar.loan.occupancy.code,
      lar.loan.amount,
      lar.action.actionTakenType.code,
      lar.action.actionTakenDate,
      lar.geography.street,
      lar.geography.city,
      lar.geography.state,
      lar.geography.zipCode,
      lar.geography.county,
      lar.geography.tract,
      convertEmptyField(lar.applicant.ethnicity.ethnicity1.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity2.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity3.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity4.code),
      convertEmptyField(lar.applicant.ethnicity.ethnicity5.code),
      lar.applicant.ethnicity.otherHispanicOrLatino,
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity1.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity2.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity3.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity4.code),
      convertEmptyField(lar.coApplicant.ethnicity.ethnicity5.code),
      lar.coApplicant.ethnicity.otherHispanicOrLatino,
      lar.applicant.ethnicity.ethnicityObserved.code,
      lar.coApplicant.ethnicity.ethnicityObserved.code,
      convertEmptyField(lar.applicant.race.race1.code),
      convertEmptyField(lar.applicant.race.race2.code),
      convertEmptyField(lar.applicant.race.race3.code),
      convertEmptyField(lar.applicant.race.race4.code),
      convertEmptyField(lar.applicant.race.race5.code),
      lar.applicant.race.otherNativeRace,
      lar.applicant.race.otherAsianRace,
      lar.applicant.race.otherPacificIslanderRace,
      convertEmptyField(lar.coApplicant.race.race1.code),
      convertEmptyField(lar.coApplicant.race.race2.code),
      convertEmptyField(lar.coApplicant.race.race3.code),
      convertEmptyField(lar.coApplicant.race.race4.code),
      convertEmptyField(lar.coApplicant.race.race5.code),
      lar.coApplicant.race.otherNativeRace,
      lar.coApplicant.race.otherAsianRace,
      lar.coApplicant.race.otherPacificIslanderRace,
      lar.applicant.race.raceObserved.code,
      lar.coApplicant.race.raceObserved.code,
      lar.applicant.sex.sexEnum.code,
      lar.coApplicant.sex.sexEnum.code,
      lar.applicant.sex.sexObservedEnum.code,
      lar.coApplicant.sex.sexObservedEnum.code,
      lar.applicant.age,
      lar.coApplicant.age,
      lar.income,
      lar.purchaserType.code,
      lar.loan.rateSpread,
      lar.hoepaStatus.code,
      lar.lienStatus.code,
      lar.applicant.creditScore,
      lar.coApplicant.creditScore,
      lar.applicant.creditScoreType.code,
      lar.applicant.otherCreditScoreModel,
      lar.coApplicant.creditScoreType.code,
      lar.coApplicant.otherCreditScoreModel,
      convertEmptyField(lar.denial.denialReason1.code),
      convertEmptyField(lar.denial.denialReason2.code),
      convertEmptyField(lar.denial.denialReason3.code),
      convertEmptyField(lar.denial.denialReason4.code),
      lar.denial.otherDenialReason,
      lar.loanDisclosure.totalLoanCosts,
      lar.loanDisclosure.totalPointsAndFees,
      lar.loanDisclosure.originationCharges,
      lar.loanDisclosure.discountPoints,
      lar.loanDisclosure.lenderCredits,
      lar.loan.interestRate,
      lar.loan.prepaymentPenaltyTerm,
      lar.loan.debtToIncomeRatio,
      lar.loan.combinedLoanToValueRatio,
      lar.loan.loanTerm,
      lar.loan.introductoryRatePeriod,
      lar.nonAmortizingFeatures.balloonPayment.code,
      lar.nonAmortizingFeatures.interestOnlyPayments.code,
      lar.nonAmortizingFeatures.negativeAmortization.code,
      lar.nonAmortizingFeatures.otherNonAmortizingFeatures.code,
      lar.property.propertyValue,
      lar.property.manufacturedHomeSecuredProperty.code,
      lar.property.manufacturedHomeLandPropertyInterest.code,
      lar.property.totalUnits,
      lar.property.multiFamilyAffordableUnits,
      lar.applicationSubmission.code,
      lar.payableToInstitution.code,
      lar.larIdentifier.NMLSRIdentifier,
      convertEmptyField(lar.AUS.aus1.code),
      convertEmptyField(lar.AUS.aus2.code),
      convertEmptyField(lar.AUS.aus3.code),
      convertEmptyField(lar.AUS.aus4.code),
      convertEmptyField(lar.AUS.aus5.code),
      lar.AUS.otherAUS,
      lar.ausResult.ausResult1.code,
      convertEmptyField(lar.ausResult.ausResult2.code),
      convertEmptyField(lar.ausResult.ausResult3.code),
      convertEmptyField(lar.ausResult.ausResult4.code),
      convertEmptyField(lar.ausResult.ausResult5.code),
      lar.ausResult.otherAusResult,
      lar.reverseMortgage.code,
      lar.lineOfCredit.code,
      lar.businessOrCommercialPurpose.code,
      ConformingLoanLimit.assignLoanLimit(lar, overallLoanLimit, countyLoanLimitsByCounty, countyLoanLimitsByState),
      EthnicityCategorization.assignEthnicityCategorization(lar),
      RaceCategorization.assignRaceCategorization(lar),
      SexCategorization.assignSexCategorization(lar),
      DwellingCategorization.assignDwellingCategorization(lar),
      LoanProductTypeCategorization.assignLoanProductTypeCategorization(lar),
      census.population,
      census.minorityPopulationPercent,
      census.medianIncome,
      census.occupiedUnits,
      census.oneToFourFamilyUnits,
      census.medianAge,
      census.tracttoMsaIncomePercent,
      isQuarterly,
      census.msaMd.toString,
      census.name
    )
  }

  private def convertEmptyField(code: Int) =
    if (code == 0) "" else code.toString

  private def getcountyLoanLimitsByCounty(year: Int) = {
    year match {
      case 2018 => countyLoanLimitsByCounty2018
      case 2019 => countyLoanLimitsByCounty2019
      case 2020 => countyLoanLimitsByCounty2020
    }
  }

  private def getcountyLoanLimitsByState(year: Int) = {
    year match {
      case 2018 => countyLoanLimitsByState2018
      case 2019 => countyLoanLimitsByState2019
      case 2020 => countyLoanLimitsByState2020
    }
  }

  private def getOverallLoanLimit(year: Int) = {
    year match {
      case 2018 => overallLoanLimit2018
      case 2019 => overallLoanLimit2019
      case 2020 => overallLoanLimit2020
    }
  }

}
