package hmda.model.filing.lar

import java.text.SimpleDateFormat
import java.util.Date

import hmda.generators.CommonGenerators._
import hmda.model.census.Census
import hmda.model.filing.lar.enums.LarEnumGenerators._
import org.scalacheck.Gen

import scala.language.implicitConversions

object LarGenerators {

  implicit def larNGen(n: Int): Gen[List[LoanApplicationRegister]] = {
    Gen.listOfN(n, larGen)
  }

  implicit def larGen: Gen[LoanApplicationRegister] = {
    for {
      larId <- larIdentifierGen
      loan <- loanGen
      larAction <- larActionGen
      geography <- geographyGen
      applicant <- applicantGen
      coApplicant <- applicantGen
      income <- intValueOrNA(Gen.choose(1, 10000000))
      purchaserType <- purchaserEnumGen
      hoepaStatus <- hOEPAStatusEnumGen
      lienStatus <- lienStatusEnumGen
      denial <- denialGen
      loanDisclosure <- loanDisclosureGen
      otherNonAmortizingFeatures <- nonAmortizingFeaturesGen
      property <- propertyGen
      applicationSubmission <- applicationSubmissionEnumGen
      payableToInstitution <- payableToInstitutionEnumGen
      aus <- automatedUnderwritingSystemGen
      ausResult <- automatedUnderwritingSystemResultGen
      reverseMortgage <- mortgageTypeEnum
      lineOfCredit <- lineOfCreditEnumGen
      businessOrCommercialPurpose <- businessOrCommercialBusinessEnumGen
    } yield
      LoanApplicationRegister(
        larId,
        loan,
        larAction,
        geography,
        applicant,
        coApplicant,
        income,
        purchaserType,
        hoepaStatus,
        lienStatus,
        denial,
        loanDisclosure,
        otherNonAmortizingFeatures,
        property,
        applicationSubmission,
        payableToInstitution,
        aus,
        ausResult,
        reverseMortgage,
        lineOfCredit,
        businessOrCommercialPurpose
      )
  }

  implicit def larIdentifierGen: Gen[LarIdentifier] = {
    for {
      lei <- stringOfN(20, Gen.alphaUpperChar)
      nmlsrIdentifier <- intValueOrNA(Gen.choose(0, Int.MaxValue))
    } yield LarIdentifier(2, lei, nmlsrIdentifier)

  }

  implicit def loanGen: Gen[Loan] = {
    for {
      uli <- stringOfUpToN(45, Gen.alphaChar)
      applicationDate <- valueOrDefault(dateGen, "NA")
      loanType <- loanTypeEnumGen
      loanPurpose <- loanPurposeEnumGen
      constructionMethod <- constructionMethodEnumGen
      occupancy <- occupancyEnumGen
      amount <- Gen.choose(0.0, Double.MaxValue)
      term <- intValueOrNA(Gen.choose(1, Int.MaxValue))
      rateSpread <- doubleValueOrNA(Gen.choose(0.0, 1.0))
      interestRate <- doubleValueOrNA(Gen.choose(0.0, 30.0))
      prepaymentPenaltyTerm <- intValueOrNA(Gen.choose(1, Int.MaxValue))
      debtToIncomeRatio <- doubleValueOrNA(Gen.choose(0.0, 1.0))
      loanToValueRatio <- doubleValueOrNA(Gen.choose(0.0, 100.0))
      introductoryRatePeriod <- intValueOrNA(Gen.choose(1, Int.MaxValue))
    } yield
      Loan(
        uli,
        applicationDate,
        loanType,
        loanPurpose,
        constructionMethod,
        occupancy,
        amount,
        term,
        rateSpread,
        interestRate,
        prepaymentPenaltyTerm,
        debtToIncomeRatio,
        loanToValueRatio,
        introductoryRatePeriod
      )
  }

  implicit def larActionGen: Gen[LarAction] = {
    for {
      preapproval <- preapprovalEnumGen
      actionTakenType <- actionTakenTypeEnumGen
      actionTakenDate <- dateGen
    } yield LarAction(preapproval, actionTakenType, actionTakenDate)
  }

  implicit def loanDisclosureGen: Gen[LoanDisclosure] = {
    for {
      totalLoanCosts <- doubleValueOrNA(Gen.choose(0.0, Double.MaxValue))
      totalPointsAndFees <- doubleValueOrNA(Gen.choose(0.0, Double.MaxValue))
      originationCharges <- doubleValueOrNA(Gen.choose(0.0, Double.MaxValue))
      discountPoints <- doubleValueOrNA(Gen.choose(0.0, Double.MaxValue))
      lenderCredits <- doubleValueOrNA(Gen.choose(0.0, Double.MaxValue))
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
      street <- strValueOrNA(Gen.alphaStr.suchThat(!_.isEmpty))
      city <- strValueOrNA(Gen.alphaStr.suchThat(!_.isEmpty))
      state <- stateCodeGen
      zipCode <- zipCodeGen
      county <- countyGen
      tract <- tractGen
    } yield Geography(street, city, state, zipCode, county, tract)
  }

  implicit def nonAmortizingFeaturesGen: Gen[NonAmortizingFeatures] = {
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

  implicit def propertyGen: Gen[Property] = {
    for {
      propertyValue <- doubleValueOrNA(Gen.choose(1.0, Double.MaxValue))
      manufacturedHomeSecuredProperty <- manufacturedHomeSecuredPropertyEnumGen
      manufacturedHomeLandPropertyInterest <- manufacturedHomeLandPropertyInterestEnumGen
      totalUnits <- Gen.choose(1, 100)
      multiFamilyAffordableUnits <- intValueOrNA(Gen.choose(1, 1000))
    } yield
      Property(
        propertyValue,
        manufacturedHomeSecuredProperty,
        manufacturedHomeLandPropertyInterest,
        totalUnits,
        multiFamilyAffordableUnits
      )
  }

  implicit def stateCodeGen: Gen[String] = {
    strValueOrNA(Gen.oneOf(Census.states.keys.toList))
  }

  implicit def countyGen: Gen[String] = {
    intValueOrNA(stringOfN(5, Gen.numChar))
  }

  implicit def tractGen: Gen[String] = {
    intValueOrNA(stringOfN(11, Gen.numChar))
  }

  implicit def zipCodeGen: Gen[String] = {
    intValueOrNA(Gen.oneOf(zip5Gen, zipPlus4Gen))
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
      race <- raceGen
      sex <- sexGen
      age <- Gen.choose(18, 100)
      creditScore <- Gen.choose(0, 20000)
      creditScoreType <- creditScoreEnumGen
      otherCreditScoreModel <- Gen.alphaStr
    } yield
      Applicant(
        ethnicity,
        race,
        sex,
        age,
        creditScore,
        creditScoreType,
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
      other <- Gen.alphaStr
      observed <- ethnicifyObserverdEnumGen
    } yield Ethnicity(eth1, eth2, eth3, eth4, eth5, other, observed)
  }

  implicit def raceGen: Gen[Race] = {
    for {
      race1 <- raceEnumGen
      race2 <- raceEnumGen
      race3 <- raceEnumGen
      race4 <- raceEnumGen
      race5 <- raceEnumGen
      otherNative <- Gen.alphaStr
      otherAsian <- Gen.alphaStr
      otherPacific <- Gen.alphaStr
      observed <- raceObservedEnumGen
    } yield
      Race(race1,
           race2,
           race3,
           race4,
           race5,
           otherNative,
           otherAsian,
           otherPacific,
           observed)
  }

  implicit def sexGen: Gen[Sex] = {
    for {
      sexEnum <- sexEnumGen
      sexObserved <- sexObservedEnumGen
    } yield Sex(sexEnum, sexObserved)
  }

  implicit def denialGen: Gen[Denial] = {
    for {
      denial1 <- denialReasonEnumGen
      denial2 <- denialReasonEnumGen
      denial3 <- denialReasonEnumGen
      denial4 <- denialReasonEnumGen
      other <- Gen.alphaStr
    } yield Denial(denial1, denial2, denial3, denial4, other)
  }

  implicit def automatedUnderwritingSystemGen
    : Gen[AutomatedUnderwritingSystem] = {
    for {
      aus1 <- automatedUnderwritingSystemEnumGen
      aus2 <- automatedUnderwritingSystemEnumGen
      aus3 <- automatedUnderwritingSystemEnumGen
      aus4 <- automatedUnderwritingSystemEnumGen
      aus5 <- automatedUnderwritingSystemEnumGen
      other <- Gen.alphaStr
    } yield AutomatedUnderwritingSystem(aus1, aus2, aus3, aus4, aus5, other)
  }

  implicit def automatedUnderwritingSystemResultGen
    : Gen[AutomatedUnderwritingSystemResult] = {
    for {
      ausResult1 <- automatedUnderWritingSystemResultEnumGen
      ausResult2 <- automatedUnderWritingSystemResultEnumGen
      ausResult3 <- automatedUnderWritingSystemResultEnumGen
      ausResult4 <- automatedUnderWritingSystemResultEnumGen
      ausResult5 <- automatedUnderWritingSystemResultEnumGen
      other <- Gen.alphaStr
    } yield
      AutomatedUnderwritingSystemResult(ausResult1,
                                        ausResult2,
                                        ausResult3,
                                        ausResult4,
                                        ausResult5,
                                        other)
  }

  private def strValueOrNA[A](g: Gen[A]): Gen[String] =
    valueOrDefault(g, "NA")

  private def intValueOrNA[A](g: Gen[A]): Gen[String] =
    valueOrDefault(g, "NA")

  private def doubleValueOrNA[A](g: Gen[A]): Gen[String] =
    valueOrDefault(g, "NA")

  private def valueOrDefault[A](g: Gen[A], value: String) = {
    Gen.oneOf(g.map(_.toString), Gen.const(value))
  }

}
