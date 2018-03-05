package hmda.model.filing.lar

import java.text.SimpleDateFormat
import java.util.Date

import hmda.model.census.Census
import org.scalacheck.Gen
import hmda.model.filing.FilingGenerators._
import hmda.model.filing.lar.enums.LarEnumGenerators._

import scala.language.implicitConversions

object LarGenerators {

  implicit def larNGen(n: Int): Gen[List[LoanApplicationRegister]] = {
    Gen.listOfN(n, larGen)
  }

  implicit def larGen: Gen[LoanApplicationRegister] = {
    for {
      lei <- Gen.option(stringOfN(20, Gen.alphaChar))
      loan <- loanGen
      preapproval <- preapprovalEnumGen
      actionTakenType <- actionTakenTypeEnumGen
      actionTakenDate <- dateGen
      geography <- geographyGen
      applicant <- applicantGen
      coApplicant <- applicantGen
      purchaserType <- purchaserEnumGen
      rateSpread <- valueOrNA(Gen.choose(0.0, 1.0))
      hoepaStatus <- hOEPAStatusEnumGen
      lienStatus <- lienStatusEnumGen
      denial <- denialGen
      otherDenialReason <- Gen.alphaStr
      loanDisclosure <- loanDisclosureGen
      interestRate <- Gen.option(valueOrNA(Gen.choose(0.0, 30.0)))
      prepaymentPenaltyTerm <- valueOrNA(Gen.alphaNumStr)

      loanToValueRatio <- Gen.option(valueOrNA(Gen.choose(0.0, 100.0)))
      loanTerm <- valueOrNA(Gen.alphaNumStr)
      introductoryRatePeriod <- valueOrNA(Gen.alphaNumStr)
      otherNonAmortizingFeatures <- otherNonAmortizingFeaturesGen
      propertyValue <- valueOrNA(Gen.alphaNumStr)
      manufacturedHomeSecuredProperty <- Gen.option(
        manufacturedHomeSecuredPropertyEnumGen)
      manufacturedHomeLandPropertyInterestEnum <- Gen.option(
        manufacturedHomeLandPropertyInterestEnumGen)
      totalUnits <- Gen.option(Gen.choose(1, 100))
      multiFamilyAffordableUnits <- Gen.option(valueOrNA(Gen.choose(1, 1000)))
      applicationSubmission <- applicationSubmissionEnumGen
      payableToInstitution <- payableToInstitutionEnumGen
      nmlsrIdentified <- valueOrNA(Gen.alphaNumStr)
      aus <- Gen.option(automatedUnderwritingSystemGen)
      otherAUS <- Gen.option(Gen.alphaStr)
      ausResult <- Gen.option(automatedUnderwritingSystemResult)
      otherAusResult <- Gen.option(Gen.alphaStr)
      reverseMortgage <- Gen.option(mortgageTypeEnum)
      lineOfCredit <- Gen.option(lineOfCreditEnumGen)
      businessOrCommercialPurpose <- Gen.option(
        businessOrCommercialBusinessEnumGen)
    } yield
      LoanApplicationRegister(
        2,
        lei,
        loan,
        preapproval,
        actionTakenType,
        actionTakenDate,
        geography,
        applicant,
        coApplicant,
        purchaserType,
        rateSpread,
        hoepaStatus,
        lienStatus,
        denial,
        otherDenialReason,
        loanDisclosure,
        interestRate,
        prepaymentPenaltyTerm,
        loanToValueRatio,
        introductoryRatePeriod,
        otherNonAmortizingFeatures,
        propertyValue,
        manufacturedHomeSecuredProperty,
        manufacturedHomeLandPropertyInterestEnum,
        totalUnits,
        multiFamilyAffordableUnits,
        applicationSubmission,
        payableToInstitution,
        nmlsrIdentified,
        aus,
        otherAUS,
        ausResult,
        otherAusResult,
        reverseMortgage,
        lineOfCredit,
        businessOrCommercialPurpose
      )
  }

  implicit def loanGen: Gen[Loan] = {
    for {
      uli <- Gen.option(stringOfUpToN(45, Gen.alphaChar))
      applicationDate <- valueOrNA(dateGen)
      loanType <- loanTypeEnumGen
      loanPurpose <- loanPurposeEnumGen
      constructionMethod <- constructionMethodEnumGen
      occupancy <- occupancyEnumGen
      amount <- Gen.choose(0.0, Double.MaxValue)
      term <- valueOrNA(Gen.choose(0.0, Double.MaxValue))
    } yield
      Loan(uli,
           applicationDate,
           loanType,
           loanPurpose,
           constructionMethod,
           occupancy,
           amount,
           term)
  }

  implicit def loanDisclosureGen: Gen[LoanDisclosure] = {
    for {
      totalLoanCosts <- valueOrNA(Gen.choose(0.0, Double.MaxValue))
      totalPointsAndFees <- valueOrNA(Gen.choose(0.0, Double.MaxValue))
      originationCharges <- Gen.option(
        valueOrNA(Gen.choose(0.0, Double.MaxValue)))
      discountPoints <- Gen.option(valueOrNA(Gen.choose(0.0, Double.MaxValue)))
      lenderCredits <- Gen.option(valueOrNA(Gen.choose(0.0, Double.MaxValue)))
    } yield {
      LoanDisclosure(totalLoanCosts,
                     totalPointsAndFees,
                     originationCharges,
                     discountPoints,
                     lenderCredits)
    }
  }

  implicit def dateGen: Gen[Int] = {
    val dateFormat = new SimpleDateFormat("yyyyMMdd")
    val beginDate = dateFormat.parse("20180101")
    val endDate = dateFormat.parse("20201231")
    for {
      randomDate <- Gen.choose(beginDate.getTime, endDate.getTime)
    } yield dateFormat.format(new Date(randomDate)).toInt
  }

  implicit def geographyGen: Gen[Geography] = {
    for {
      street <- valueOrNA(Gen.alphaStr)
      city <- valueOrNA(Gen.alphaStr)
      state <- stateCodeGen
      zipCode <- zipCodeGen
      county <- countyGen
      tract <- tractGen
    } yield Geography(street, city, state, zipCode, county, tract)
  }

  implicit def otherNonAmortizingFeaturesGen: Gen[NonAmortizingFeatures] = {
    for {
      balloonPayment <- ballonPaymentEnumGen
      interestOnlyPayments <- interestOnlyPayementsEnumGen
      negativeAmortization <- negativeAmortizationEnumGen
      otherNonAmortizingFeatures <- otherNonAmortizingFeaturesEnumGen
    } yield {
      NonAmortizingFeatures(
        balloonPayment,
        interestOnlyPayments,
        negativeAmortization,
        otherNonAmortizingFeatures
      )
    }
  }

  implicit def stateCodeGen: Gen[String] = {
    valueOrNA(Gen.oneOf(Census.states.keys.toList))
  }

  implicit def countyGen: Gen[String] = {
    valueOrNA(stringOfN(5, Gen.numChar))
  }

  implicit def tractGen: Gen[String] = {
    valueOrNA(stringOfN(11, Gen.numChar))
  }

  implicit def zipCodeGen: Gen[String] = {
    valueOrNA(Gen.oneOf(zip5Gen, zipPlus4Gen))
  }

  private def zip5Gen: Gen[String] = {
    stringOfN(5, Gen.numChar)
  }

  private def zipPlus4Gen: Gen[String] = {
    for {
      zip <- zip5Gen
      plus <- stringOfN(4, Gen.numChar)
      sep = "-"
    } yield List(zip, plus).mkString(sep)
  }

  implicit def applicantGen: Gen[Applicant] = {
    for {
      ethnicity <- ethnicityGen
      otherHispanicOrLatino <- Gen.alphaStr
      ethnicityObserved <- ethnicifyObserverdEnumGen
      race <- raceGen
      raceObserved <- raceObservedEnumGen
      otherNativeRace <- Gen.alphaStr
      otherAsianRace <- Gen.alphaStr
      otherPacificRace <- Gen.alphaStr
      sex <- sexEnumGen
      coSex <- sexEnumGen
      sexObserved <- sexObservedEnumGen
      coSexObserved <- sexObservedEnumGen
      age <- Gen.choose(18, 100)
      coAge <- Gen.choose(18, 100)
      income <- valueOrNA(Gen.choose(0, 1000))
      creditScore <- Gen.choose(0, Int.MaxValue)
      creditScoreType <- creditScoreEnumGen
      debtToIncomeRatio <- Gen.option(valueOrNA(Gen.choose(0.0, 5.0)))
      otherCreditScoreModel <- Gen.alphaStr
    } yield
      Applicant(
        ethnicity,
        otherHispanicOrLatino,
        ethnicityObserved,
        race,
        raceObserved,
        otherNativeRace,
        otherAsianRace,
        otherPacificRace,
        sex,
        coSex,
        sexObserved,
        coSexObserved,
        age,
        coAge,
        income,
        creditScore,
        creditScoreType,
        debtToIncomeRatio,
        otherCreditScoreModel
      )
  }

  implicit def ethnicityGen: Gen[Ethnicity] = {
    for {
      eth1 <- ethnicityEnumGen
      eth2 <- ethnicityEnumGen
      eth3 <- ethnicityEnumGen
      eth4 <- ethnicityEnumGen
      eth5 <- ethnicityEnumGen
    } yield Ethnicity(eth1, eth2, eth3, eth4, eth5)
  }

  implicit def raceGen: Gen[Race] = {
    for {
      race1 <- raceEnumGen
      race2 <- raceEnumGen
      race3 <- raceEnumGen
      race4 <- raceEnumGen
      race5 <- raceEnumGen
    } yield Race(race1, race2, race3, race4, race5)
  }

  implicit def denialGen: Gen[Denial] = {
    for {
      denial1 <- denialReasonEnumGen
      denial2 <- denialReasonEnumGen
      denial3 <- denialReasonEnumGen
      denial4 <- denialReasonEnumGen
    } yield Denial(denial1, denial2, denial3, denial4)
  }

  implicit def automatedUnderwritingSystemGen
    : Gen[AutomatedUnderwritingSystem] = {
    for {
      aus1 <- automatedUnderwritingSystemEnumGen
      aus2 <- automatedUnderwritingSystemEnumGen
      aus3 <- automatedUnderwritingSystemEnumGen
      aus4 <- automatedUnderwritingSystemEnumGen
      aus5 <- automatedUnderwritingSystemEnumGen
    } yield AutomatedUnderwritingSystem(aus1, aus2, aus3, aus4, aus5)
  }

  implicit def automatedUnderwritingSystemResult
    : Gen[AutomatedUnderwritingSystemResult] = {
    for {
      ausResult1 <- automatedUnderWritingSystemResultEnumGen
      ausResult2 <- automatedUnderWritingSystemResultEnumGen
      ausResult3 <- automatedUnderWritingSystemResultEnumGen
      ausResult4 <- automatedUnderWritingSystemResultEnumGen
      ausResult5 <- automatedUnderWritingSystemResultEnumGen
    } yield
      AutomatedUnderwritingSystemResult(ausResult1,
                                        ausResult2,
                                        ausResult3,
                                        ausResult4,
                                        ausResult5)
  }

  private def valueOrNA[A](g: Gen[A]): Gen[String] = valueOrDefault("NA")

  private def valueOrDefault[A](g: Gen[A], value: String = "") = {
    Gen.oneOf(g.map(_.toString), Gen.const(value))
  }

}
