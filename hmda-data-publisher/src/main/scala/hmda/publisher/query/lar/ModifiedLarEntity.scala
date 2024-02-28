package hmda.publisher.query.lar

import hmda.util.conversion.ColumnDataFormatter
import io.chrisdavenport.cormorant
import io.chrisdavenport.cormorant.CSV
import io.chrisdavenport.cormorant.implicits._
import hmda.parser.filing.ts.TsCsvParser.extractOpt
import hmda.util.PsvParsingCompanion
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.parser._
import cats.implicits._
import java.util.UUID
import java.time.Instant
case class ModifiedLarPartOne(
                               filingYear: Option[Int] = Some(0),
                               lei: String = "",
                               msaMd: Option[Int] = Some(0),
                               state: Option[String] = Some(""),
                               county: Option[String] = Some(""),
                               tract: Option[String] = Some(""),
                               conformingLoanLimit: Option[String] = Some(""),
                               loanFlag: Option[String] = Some(""),
                               loanProductType: Option[String] = Some(""),
                               dwellingCategory: Option[String] = Some(""),
                               ethnicityCategorization: Option[String] = Some(""),
                               raceCategorization: Option[String] = Some(""),
                               sexCategorization: Option[String] = Some(""),
                               actionTakenType: Option[Int] = Some(0),
                               purchaserType: Option[Int] = Some(0),
                               preapproval: Option[Int] = Some(0),
                               loanType: Option[Int] = Some(0),
                               loanPurpose: Option[Int] = Some(0),
                               lienStatus: Option[Int] = Some(0)
                             ) extends ColumnDataFormatter {
  def isEmpty: Boolean = lei == ""

  def toPublicPSV: String =
    s"${extractOpt(filingYear)}|$lei|${extractOpt(msaMd)}|${extractOpt(state)}|${extractOpt(county)}|" +
      s"${extractOpt(tract)}|${extractOpt(conformingLoanLimit)}|${extractOpt(loanProductType)}|${extractOpt(dwellingCategory)}|${extractOpt(ethnicityCategorization)}|" +
      s"${extractOpt(raceCategorization)}|${extractOpt(sexCategorization)}|${extractOpt(actionTakenType)}|${extractOpt(purchaserType)}|" +
      s"${extractOpt(preapproval)}|${extractOpt(loanType)}|${extractOpt(loanPurpose)}|${extractOpt(lienStatus)}|"

  def toPublicCSV: String =
    s"${extractOpt(filingYear)},$lei,${extractOpt(msaMd)},${extractOpt(state)},${extractOpt(county)}," +
      s"${extractOpt(tract)},${extractOpt(conformingLoanLimit)},${extractOpt(loanProductType)},${extractOpt(dwellingCategory)},${extractOpt(ethnicityCategorization)}," +
      s"${extractOpt(raceCategorization)},${extractOpt(sexCategorization)},${extractOpt(actionTakenType)},${extractOpt(purchaserType)}," +
      s"${extractOpt(preapproval)},${extractOpt(loanType)},${extractOpt(loanPurpose)},${extractOpt(lienStatus)},"

}
object ModifiedLarPartOne extends PsvParsingCompanion[ModifiedLarPartOne] {
  override val psvReader: cormorant.Read[ModifiedLarPartOne] = { (a: CSV.Row) =>
    for {
      (rest, filingYear)              <- enforcePartialRead(readNext[Option[Int]], a)
      (rest, lei)                     <- enforcePartialRead(readNext[String], rest)
      (rest, msaMd)                   <- enforcePartialRead(readNext[Option[Int]], rest)
      (rest, state)                   <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, county)                  <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, tract)                   <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, conformingLoanLimit)     <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, loanProductType)         <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, dwellingCategory)        <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, ethnicityCategorization) <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, raceCategorization)      <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, sexCategorization)       <- enforcePartialRead(readNext[Option[String]], rest)
      (rest, actionTakenType)         <- enforcePartialRead(readNext[Option[Int]], rest)
      (rest, purchaserType)           <- enforcePartialRead(readNext[Option[Int]], rest)
      (rest, preapproval)             <- enforcePartialRead(readNext[Option[Int]], rest)
      (rest, loanType)                <- enforcePartialRead(readNext[Option[Int]], rest)
      (rest, loanPurpose)             <- enforcePartialRead(readNext[Option[Int]], rest)
      lienStatusOrMore <- readNext[Option[Int]].readPartial(rest)
    } yield {
      def create(lienStatus: Option[Int]) = ModifiedLarPartOne(
        filingYear = filingYear,
        lei = lei,
        msaMd = msaMd,
        state = state,
        county = county,
        tract = tract,
        conformingLoanLimit = conformingLoanLimit,
        loanProductType = loanProductType,
        dwellingCategory = dwellingCategory,
        ethnicityCategorization = ethnicityCategorization,
        raceCategorization = raceCategorization,
        sexCategorization = sexCategorization,
        actionTakenType = actionTakenType,
        purchaserType = purchaserType,
        preapproval = preapproval,
        loanType = loanType,
        loanPurpose = loanPurpose,
        lienStatus = lienStatus,
      )

      lienStatusOrMore match {
        case Left((more, lienStatus)) => Left(more -> create(lienStatus))
        case Right(lienStatus) => Right(create(lienStatus))
      }
    }
  }
}

case class ModifiedLarPartTwo(
                               reverseMortgage: Option[Int] = Some(0),
                               lineOfCredits: Option[Int] = Some(0),
                               businessOrCommercial: Option[Int] = Some(0),
                               loanAmount: Option[String] = Some(""),
                               loanValueRatio: Option[String] = Some(""),
                               interestRate: Option[String] = Some(""),
                               rateSpread: Option[String] = Some(""),
                               hoepaStatus: Option[Int] = Some(0),
                               totalLoanCosts: Option[String] = Some(""),
                               totalPoints: Option[String] = Some(""),
                               originationCharges: Option[String] = Some(""),
                               discountPoints: Option[String] = Some(""),
                               lenderCredits: Option[String] = Some(""),
                               loanTerm: Option[String] = Some(""),
                               paymentPenalty: Option[String] = Some(""),
                               rateSpreadIntro: Option[String] = Some(""),
                               amortization: Option[Int] = Some(0),
                               insertOnlyPayment: Option[Int] = Some(0)
                             ) extends ColumnDataFormatter {

  def toPublicPSV: String =
    s"${extractOpt(reverseMortgage)}|${extractOpt(lineOfCredits)}|${extractOpt(businessOrCommercial)}|" +
      s"${extractOpt(loanAmount)}" +
      s"|${extractOpt(loanValueRatio)}|${extractOpt(interestRate)}|${extractOpt(rateSpread)}|${extractOpt(hoepaStatus)}|" +
      s"${extractOpt(totalLoanCosts)}|${extractOpt(totalPoints)}|${extractOpt(originationCharges)}|${extractOpt(discountPoints)}|" +
      s"${extractOpt(lenderCredits)}|${extractOpt(loanTerm)}|${extractOpt(paymentPenalty)}|${extractOpt(rateSpreadIntro)}" +
      s"|${extractOpt(amortization)}|${extractOpt(insertOnlyPayment)}|"

  def toPublicCSV: String =
    s"${extractOpt(reverseMortgage)},${extractOpt(lineOfCredits)},${extractOpt(businessOrCommercial)}," +
      s"${extractOpt(loanAmount)}" +
      s",${extractOpt(loanValueRatio)},${extractOpt(interestRate)},${extractOpt(rateSpread)},${extractOpt(hoepaStatus)}," +
      s"${extractOpt(totalLoanCosts)},${extractOpt(totalPoints)},${extractOpt(originationCharges)},${extractOpt(discountPoints)}," +
      s"${extractOpt(lenderCredits)},${extractOpt(loanTerm)},${extractOpt(paymentPenalty)},${extractOpt(rateSpreadIntro)}" +
      s",${extractOpt(amortization)},${extractOpt(insertOnlyPayment)},"

}
object ModifiedLarPartTwo extends PsvParsingCompanion[ModifiedLarPartTwo] {
  override val psvReader: cormorant.Read[ModifiedLarPartTwo] = cormorant.generic.semiauto.deriveRead
}

case class ModifiedLarPartThree(
                                 balloonPayment: Option[Int] = Some(0),
                                 otherAmortization: Option[Int] = Some(0),
                                 propertyValue: String = "",
                                 constructionMethod: Option[String] = Some(""),
                                 occupancyType: Option[Int] = Some(0),
                                 homeSecurityPolicy: Option[Int] = Some(0),
                                 landPropertyInterest: Option[Int] = Some(0),
                                 totalUnits: Option[String] = Some(""),
                                 mfAffordable: Option[String] = Some(""),
                                 income: Option[String] = Some(""),
                                 debtToIncome: Option[String] = Some(""),
                                 creditScoreTypeApplicant: Option[Int] = Some(0),
                                 creditScoreTypeCoApplicant: Option[Int] = Some(0),
                                 ethnicityApplicant1: Option[String] = Some(""),
                                 ethnicityApplicant2: Option[String] = Some(""),
                                 ethnicityApplicant3: Option[String] = Some(""),
                                 ethnicityApplicant4: Option[String] = Some("")
                               ) extends ColumnDataFormatter {

  def toPublicPSV: String =
    s"${extractOpt(balloonPayment)}|" +
      s"${extractOpt(otherAmortization)}|" +
      toBigDecimalString(propertyValue) + "|" +
      s"${extractOpt(constructionMethod)}|${extractOpt(occupancyType)}|" +
      s"${extractOpt(homeSecurityPolicy)}|${extractOpt(landPropertyInterest)}|${extractOpt(totalUnits)}|${extractOpt(mfAffordable)}|" +
      s"${extractOpt(income)}|${extractOpt(debtToIncome)}|${extractOpt(creditScoreTypeApplicant)}|" +
      s"${extractOpt(creditScoreTypeCoApplicant)}|${extractOpt(ethnicityApplicant1)}|${extractOpt(ethnicityApplicant2)}" +
      s"|${extractOpt(ethnicityApplicant3)}|${extractOpt(ethnicityApplicant4)}|"

  def toPublicCSV: String =
    s"${extractOpt(balloonPayment)}," +
      s"${extractOpt(otherAmortization)}," +
      toBigDecimalString(propertyValue) + "," +
      s"${extractOpt(constructionMethod)},${extractOpt(occupancyType)}," +
      s"${extractOpt(homeSecurityPolicy)},${extractOpt(landPropertyInterest)},${extractOpt(totalUnits)},${extractOpt(mfAffordable)}," +
      s"${extractOpt(income)},${extractOpt(debtToIncome)},${extractOpt(creditScoreTypeApplicant)}," +
      s"${extractOpt(creditScoreTypeCoApplicant)},${extractOpt(ethnicityApplicant1)},${extractOpt(ethnicityApplicant2)}" +
      s",${extractOpt(ethnicityApplicant3)},${extractOpt(ethnicityApplicant4)},"

}
object ModifiedLarPartThree extends PsvParsingCompanion[ModifiedLarPartThree] {
  override val psvReader: cormorant.Read[ModifiedLarPartThree] = cormorant.generic.semiauto.deriveRead
}

case class ModifiedLarPartFour(
                                ethnicityApplicant5: Option[String] = Some(""),
                                ethnicityCoApplicant1: Option[String] = Some(""),
                                ethnicityCoApplicant2: Option[String] = Some(""),
                                ethnicityCoApplicant3: Option[String] = Some(""),
                                ethnicityCoApplicant4: Option[String] = Some(""),
                                ethnicityCoApplicant5: Option[String] = Some(""),
                                ethnicityObservedApplicant: Option[Int] = Some(0),
                                ethnicityObservedCoApplicant: Option[Int] = Some(0),
                                raceApplicant1: Option[String] = Some(""),
                                raceApplicant2: Option[String] = Some(""),
                                raceApplicant3: Option[String] = Some(""),
                                raceApplicant4: Option[String] = Some(""),
                                raceApplicant5: Option[String] = Some(""),
                                raceCoApplicant1: Option[String] = Some(""),
                                raceCoApplicant2: Option[String] = Some(""),
                                raceCoApplicant3: Option[String] = Some(""),
                                raceCoApplicant4: Option[String] = Some("")
                              ) extends ColumnDataFormatter {

  def toPublicPSV: String =
    s"${extractOpt(ethnicityApplicant5)}|${extractOpt(ethnicityCoApplicant1)}|${extractOpt(ethnicityCoApplicant2)}|${extractOpt(ethnicityCoApplicant3)}|" +
      s"${extractOpt(ethnicityCoApplicant4)}|${extractOpt(ethnicityCoApplicant5)}|${extractOpt(ethnicityObservedApplicant)}|${extractOpt(ethnicityObservedCoApplicant)}|" +
      s"${extractOpt(raceApplicant1)}|${extractOpt(raceApplicant2)}|" +
      s"${extractOpt(raceApplicant3)}|${extractOpt(raceApplicant4)}|${extractOpt(raceApplicant5)}|${extractOpt(raceCoApplicant1)}|" +
      s"${extractOpt(raceCoApplicant2)}|${extractOpt(raceCoApplicant3)}|${extractOpt(raceCoApplicant4)}|"

  def toPublicCSV: String =
    s"${extractOpt(ethnicityApplicant5)},${extractOpt(ethnicityCoApplicant1)},${extractOpt(ethnicityCoApplicant2)},${extractOpt(ethnicityCoApplicant3)}," +
      s"${extractOpt(ethnicityCoApplicant4)},${extractOpt(ethnicityCoApplicant5)},${extractOpt(ethnicityObservedApplicant)},${extractOpt(ethnicityObservedCoApplicant)}," +
      s"${extractOpt(raceApplicant1)},${extractOpt(raceApplicant2)}," +
      s"${extractOpt(raceApplicant3)},${extractOpt(raceApplicant4)},${extractOpt(raceApplicant5)},${extractOpt(raceCoApplicant1)}," +
      s"${extractOpt(raceCoApplicant2)},${extractOpt(raceCoApplicant3)},${extractOpt(raceCoApplicant4)},"

}
object ModifiedLarPartFour extends PsvParsingCompanion[ModifiedLarPartFour] {
  override val psvReader: cormorant.Read[ModifiedLarPartFour] = cormorant.generic.semiauto.deriveRead
}

case class ModifiedLarPartFive(
                                raceCoApplicant5: Option[String] = Some(""),
                                raceObservedApplicant: Option[Int] = Some(0),
                                raceObservedCoApplicant: Option[Int] = Some(0),
                                sexApplicant: Option[Int] = Some(0),
                                sexCoApplicant: Option[Int] = Some(0),
                                observedSexApplicant: Option[Int] = Some(0),
                                observedSexCoApplicant: Option[Int] = Some(0),
                                ageApplicant: Option[String] = Some(""),
                                ageCoApplicant: Option[String] = Some(""),
                                applicantAgeGreaterThan62: Option[String] = Some(""),
                                coapplicantAgeGreaterThan62: Option[String] = Some(""),
                                applicationSubmission: Option[Int] = Some(0),
                                payable: Option[Int] = Some(0),
                                aus1: Option[Int] = Some(0),
                                aus2: Option[Int] = Some(0),
                                aus3: Option[Int] = Some(0),
                                aus4: Option[Int] = Some(0)
                              ) extends ColumnDataFormatter {

  def toPublicPSV: String =
    s"${extractOpt(raceCoApplicant5)}|${extractOpt(raceObservedApplicant)}|${extractOpt(raceObservedCoApplicant)}|${extractOpt(sexApplicant)}|${extractOpt(
      sexCoApplicant
    )}|${extractOpt(observedSexApplicant)}|${extractOpt(observedSexCoApplicant)}|" +
      s"${extractOpt(ageApplicant)}|${extractOpt(ageCoApplicant)}|${extractOpt(applicantAgeGreaterThan62)}|${extractOpt(coapplicantAgeGreaterThan62)}|${extractOpt(applicationSubmission)}|" +
      s"${extractOpt(payable)}|${extractOpt(aus1)}|${extractOpt(aus2)}|${extractOpt(aus3)}|${extractOpt(aus4)}|"

  def toPublicCSV: String =
    s"${extractOpt(raceCoApplicant5)},${extractOpt(raceObservedApplicant)},${extractOpt(raceObservedCoApplicant)},${extractOpt(sexApplicant)},${extractOpt(
      sexCoApplicant
    )},${extractOpt(observedSexApplicant)},${extractOpt(observedSexCoApplicant)}," +
      s"${extractOpt(ageApplicant)},${extractOpt(ageCoApplicant)},${extractOpt(applicantAgeGreaterThan62)},${extractOpt(coapplicantAgeGreaterThan62)},${extractOpt(applicationSubmission)}," +
      s"${extractOpt(payable)},${extractOpt(aus1)},${extractOpt(aus2)},${extractOpt(aus3)},${extractOpt(aus4)},"

}
object ModifiedLarPartFive extends PsvParsingCompanion[ModifiedLarPartFive] {
  override val psvReader: cormorant.Read[ModifiedLarPartFive] = cormorant.generic.semiauto.deriveRead
}

case class ModifiedLarPartSix(
                               aus5: Option[Int] = Some(0),
                               denialReason1: Option[Int] = Some(0),
                               denialReason2: Option[Int] = Some(0),
                               denialReason3: Option[Int] = Some(0),
                               denialReason4: Option[Int] = Some(0),
                               population: Option[String] = Some(""),
                               minorityPopulationPercent: Option[String] = Some(""),
                               ffiecMedFamIncome: Option[String] = Some(""),
                               medianIncomePercentage: Option[Double] = Some(0.0),
                               ownerOccupiedUnits: Option[String] = Some(""),
                               oneToFourFamUnits: Option[String] = Some(""),
                               medianAge: Option[Int] = Some(0)
                             ) extends ColumnDataFormatter {

  def toPublicPSV: String =
    s"${extractOpt(aus5)}|" +
      s"${extractOpt(denialReason1)}|${extractOpt(denialReason2)}|${extractOpt(denialReason3)}|${extractOpt(denialReason4)}|${extractOpt(population)}|" +
      s"${extractOpt(minorityPopulationPercent)}|${extractOpt(ffiecMedFamIncome)}|${extractOpt(medianIncomePercentage)}|" +
      s"${extractOpt(ownerOccupiedUnits)}|${extractOpt(oneToFourFamUnits)}|${extractOpt(medianAge)}"

  def toPublicCSV: String =
    s"${extractOpt(aus5)}," +
      s"${extractOpt(denialReason1)},${extractOpt(denialReason2)},${extractOpt(denialReason3)},${extractOpt(denialReason4)},${extractOpt(population)}," +
      s"${extractOpt(minorityPopulationPercent)},${extractOpt(ffiecMedFamIncome)},${extractOpt(medianIncomePercentage)}," +
      s"${extractOpt(ownerOccupiedUnits)},${extractOpt(oneToFourFamUnits)},${extractOpt(medianAge)}"

}
object ModifiedLarPartSix extends PsvParsingCompanion[ModifiedLarPartSix] {
  override val psvReader: cormorant.Read[ModifiedLarPartSix] = cormorant.generic.semiauto.deriveRead
}

case class ModifiedLarEntityImpl(
                                  mlarPartOne: ModifiedLarPartOne,
                                  mlarPartTwo: ModifiedLarPartTwo,
                                  mlarPartThree: ModifiedLarPartThree,
                                  mlarPartFour: ModifiedLarPartFour,
                                  mlarPartFive: ModifiedLarPartFive,
                                  mlarPartSix: ModifiedLarPartSix
                                ) {

  def toPublicPSV: String =
    (mlarPartOne.toPublicPSV +
      mlarPartTwo.toPublicPSV +
      mlarPartThree.toPublicPSV +
      mlarPartFour.toPublicPSV +
      mlarPartFive.toPublicPSV +
      mlarPartSix.toPublicPSV).replaceAll("(\r\n)|\r|\n", "")

  def toPublicCSV: String =
    (mlarPartOne.toPublicCSV +
      mlarPartTwo.toPublicCSV +
      mlarPartThree.toPublicCSV +
      mlarPartFour.toPublicCSV +
      mlarPartFive.toPublicCSV +
      mlarPartSix.toPublicCSV).replaceAll("(\r\n)|\r|\n", "")



  def toCombinedMLAR(delimiter: String): String = {

   val combinedMlar=  s"${extractOpt(mlarPartOne.filingYear)}${delimiter}" +
      s"${mlarPartOne.lei}${delimiter}" +
      s"${extractOpt(mlarPartOne.loanType)}${delimiter}" +
      s"${extractOpt(mlarPartOne.loanPurpose)}${delimiter}" +
      s"${extractOpt(mlarPartOne.preapproval)}${delimiter}" +
      s"${extractOpt(mlarPartThree.constructionMethod)}${delimiter}" +
      s"${extractOpt(mlarPartThree.occupancyType)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.loanAmount)}${delimiter}" +
      s"${extractOpt(mlarPartOne.actionTakenType)}${delimiter}" +
      s"${extractOpt(mlarPartOne.state)}${delimiter}" +
      s"${extractOpt(mlarPartOne.county)}${delimiter}" +
      s"${extractOpt(mlarPartOne.tract)}${delimiter}" +
      s"${extractOpt(mlarPartThree.ethnicityApplicant1)}${delimiter}" +
      s"${extractOpt(mlarPartThree.ethnicityApplicant2)}${delimiter}" +
      s"${extractOpt(mlarPartThree.ethnicityApplicant3)}${delimiter}" +
      s"${extractOpt(mlarPartThree.ethnicityApplicant4)}${delimiter}" +
      s"${extractOpt(mlarPartFour.ethnicityApplicant5)}${delimiter}" +
      s"${extractOpt(mlarPartFour.ethnicityCoApplicant1)}${delimiter}" +
      s"${extractOpt(mlarPartFour.ethnicityCoApplicant2)}${delimiter}" +
      s"${extractOpt(mlarPartFour.ethnicityCoApplicant3)}${delimiter}" +
      s"${extractOpt(mlarPartFour.ethnicityCoApplicant4)}${delimiter}" +
      s"${extractOpt(mlarPartFour.ethnicityCoApplicant5)}${delimiter}" +
      s"${extractOpt(mlarPartFour.ethnicityObservedApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFour.ethnicityObservedCoApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceApplicant1)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceApplicant2)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceApplicant3)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceApplicant4)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceApplicant5)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceCoApplicant1)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceCoApplicant2)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceCoApplicant3)}${delimiter}" +
      s"${extractOpt(mlarPartFour.raceCoApplicant4)}${delimiter}" +
      s"${extractOpt(mlarPartFive.raceCoApplicant5)}${delimiter}" +
      s"${extractOpt(mlarPartFive.raceObservedApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFive.raceObservedCoApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFive.sexApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFive.sexCoApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFive.observedSexApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFive.observedSexCoApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFive.ageApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFive.applicantAgeGreaterThan62)}${delimiter}" +
      s"${extractOpt(mlarPartFive.ageCoApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartFive.coapplicantAgeGreaterThan62)}${delimiter}" +
      s"${extractOpt(mlarPartThree.income)}${delimiter}" +
      s"${extractOpt(mlarPartOne.purchaserType)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.rateSpread)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.hoepaStatus)}${delimiter}" +
      s"${extractOpt(mlarPartOne.lienStatus)}${delimiter}" +
      s"${extractOpt(mlarPartThree.creditScoreTypeApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartThree.creditScoreTypeCoApplicant)}${delimiter}" +
      s"${extractOpt(mlarPartSix.denialReason1)}${delimiter}" +
      s"${extractOpt(mlarPartSix.denialReason2)}${delimiter}" +
      s"${extractOpt(mlarPartSix.denialReason3)}${delimiter}" +
      s"${extractOpt(mlarPartSix.denialReason4)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.totalLoanCosts)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.totalPoints)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.originationCharges)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.discountPoints)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.lenderCredits)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.interestRate)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.paymentPenalty)}${delimiter}" +
      s"${extractOpt(mlarPartThree.debtToIncome)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.loanValueRatio)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.loanTerm)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.rateSpreadIntro)}${delimiter}" +
      s"${extractOpt(mlarPartThree.balloonPayment)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.insertOnlyPayment)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.amortization)}${delimiter}" +
      s"${extractOpt(mlarPartThree.otherAmortization)}${delimiter}" +
      s"${mlarPartThree.propertyValue}${delimiter}" +
      s"${extractOpt(mlarPartThree.homeSecurityPolicy)}${delimiter}" +
      s"${extractOpt(mlarPartThree.landPropertyInterest)}${delimiter}" +
      s"${extractOpt(mlarPartThree.totalUnits)}${delimiter}" +
      s"${extractOpt(mlarPartThree.mfAffordable)}${delimiter}" +
      s"${extractOpt(mlarPartFive.applicationSubmission)}${delimiter}" +
      s"${extractOpt(mlarPartFive.payable)}${delimiter}" +
      s"${extractOpt(mlarPartFive.aus1)}${delimiter}" +
      s"${extractOpt(mlarPartFive.aus2)}${delimiter}" +
      s"${extractOpt(mlarPartFive.aus3)}${delimiter}" +
      s"${extractOpt(mlarPartFive.aus4)}${delimiter}" +
      s"${extractOpt(mlarPartSix.aus5)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.reverseMortgage)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.lineOfCredits)}${delimiter}" +
      s"${extractOpt(mlarPartTwo.businessOrCommercial)}"

    combinedMlar.replaceAll("(\r\n)|\r|\n", "")
  }
}



object ModifiedLarEntityImpl extends PsvParsingCompanion[ModifiedLarEntityImpl] {
  override val psvReader: cormorant.Read[ModifiedLarEntityImpl] = { (a: CSV.Row) =>
    (for {
      (rest, p1) <- enforcePartialRead(ModifiedLarPartOne.psvReader, a)
      (rest, p2) <- enforcePartialRead(ModifiedLarPartTwo.psvReader, rest)
      (rest, p3) <- enforcePartialRead(ModifiedLarPartThree.psvReader, rest)
      (rest, p4) <- enforcePartialRead(ModifiedLarPartFour.psvReader, rest)
      (rest, p5) <- enforcePartialRead(ModifiedLarPartFive.psvReader, rest)
      p6         <- ModifiedLarPartSix.psvReader.read(rest)
    } yield ModifiedLarEntityImpl(p1, p2, p3, p4, p5, p6)).map(Right(_))
  }
}