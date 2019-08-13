package hmda.regulator.query.lar

import java.sql.Timestamp

import hmda.util.conversion.ColumnDataFormatter

case class ModifiedLarPartOne(
    filingYear: Option[Int] = Some(0),
    lei: String = "",
    msaMd: Option[Int] = Some(0),
    state: Option[String] = Some(""),
    county: Option[String] = Some(""),
    tract: Option[String] = Some(""),
    conformingLoanLimit: Option[String] = Some(""),
    loanFlag: Option[String] = Some(""),
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

  def toPublicPSV: String = {
    s"${extractOpt(filingYear)}|$lei|${extractOpt(msaMd)}|${extractOpt(state)}|${extractOpt(county)}|" +
      s"${extractOpt(tract)}|${extractOpt(conformingLoanLimit)}|${extractOpt(
        loanFlag)}|${extractOpt(ethnicityCategorization)}|" +
      s"|${extractOpt(raceCategorization)}|${extractOpt(sexCategorization)}|${extractOpt(
        actionTakenType)}|${extractOpt(purchaserType)}|" +
      s"${extractOpt(preapproval)}|${extractOpt(loanType)}|${extractOpt(
        loanPurpose)}|${extractOpt(lienStatus)}|"
  }

}
case class ModifiedLarPartTwo(reverseMortgage: Option[Int] = Some(0),
                              lineOfCredits: Option[Int] = Some(0),
                              businessOrCommercial: Option[Int] = Some(0),
                              loanAmount: Double = 0.0,
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
                              insertOnlyPayment: Option[Int] = Some(0))
    extends ColumnDataFormatter {

  def toPublicPSV: String = {
    s"${extractOpt(reverseMortgage)}|${extractOpt(lineOfCredits)}|${extractOpt(businessOrCommercial)}|" +
      BigDecimal.valueOf(loanAmount).bigDecimal.toPlainString +
      s"|${extractOpt(loanValueRatio)}|${extractOpt(interestRate)}|${extractOpt(
        rateSpread)}|${extractOpt(hoepaStatus)}|" +
      s"${extractOpt(totalLoanCosts)}|${extractOpt(totalPoints)}|${extractOpt(
        originationCharges)}|${extractOpt(discountPoints)}|" +
      s"${extractOpt(lenderCredits)}|${extractOpt(loanTerm)}|${extractOpt(
        paymentPenalty)}|${extractOpt(rateSpreadIntro)}" +
      s"|${extractOpt(amortization)}|${extractOpt(insertOnlyPayment)}|"
  }

}

case class ModifiedLarPartThree(baloonPayment: Option[Int] = Some(0),
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
                                creditScoreTypeCoApplicant: Option[Int] = Some(
                                  0),
                                ethnicityApplicant1: Option[String] = Some(""),
                                ethnicityApplicant2: Option[String] = Some(""),
                                ethnicityApplicant3: Option[String] = Some(""),
                                ethnicityApplicant4: Option[String] = Some(""))
    extends ColumnDataFormatter {

  def toPublicPSV: String = {
    s"${extractOpt(baloonPayment)}|" +
      s"${extractOpt(otherAmortization)}|" +
      toBigDecimalString(propertyValue) + "|" +
      s"${extractOpt(constructionMethod)}|${extractOpt(occupancyType)}|" +
      s"${extractOpt(homeSecurityPolicy)}${extractOpt(landPropertyInterest)}|${extractOpt(
        totalUnits)}|${extractOpt(mfAffordable)}|" +
      s"${extractOpt(income)}|${extractOpt(debtToIncome)}|${extractOpt(creditScoreTypeApplicant)}|" +
      s"${extractOpt(creditScoreTypeCoApplicant)}|${extractOpt(
        ethnicityApplicant1)}|${extractOpt(ethnicityApplicant2)}" +
      s"|${extractOpt(ethnicityApplicant3)}|${extractOpt(ethnicityApplicant4)}|"
  }

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
    raceCoApplicant4: Option[String] = Some(""))
    extends ColumnDataFormatter {

  def toPublicPSV: String = {
    s"${extractOpt(ethnicityApplicant5)}|${extractOpt(ethnicityCoApplicant1)}|${extractOpt(
      ethnicityCoApplicant2)}|${extractOpt(ethnicityCoApplicant3)}|" +
      s"${extractOpt(ethnicityCoApplicant4)}|${extractOpt(ethnicityCoApplicant5)}|${extractOpt(
        ethnicityObservedApplicant)}|${extractOpt(ethnicityObservedCoApplicant)}|" +
      s"${extractOpt(raceApplicant1)}|${extractOpt(raceApplicant2)}|" +
      s"${extractOpt(raceApplicant3)}|${extractOpt(raceApplicant4)}|${extractOpt(
        raceApplicant5)}|${extractOpt(raceCoApplicant1)}|" +
      s"${extractOpt(raceCoApplicant2)}|${extractOpt(raceCoApplicant3)}|${extractOpt(raceCoApplicant4)}|"
  }

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
    aus4: Option[Int] = Some(0))
    extends ColumnDataFormatter {

  def toPublicPSV: String = {
    s"${extractOpt(raceCoApplicant5)}|${extractOpt(raceObservedApplicant)}|${extractOpt(
      raceObservedCoApplicant)}|${extractOpt(sexApplicant)}|${extractOpt(
      sexCoApplicant)}|${extractOpt(observedSexApplicant)}|${extractOpt(observedSexCoApplicant)}|" +
      s"${extractOpt(ageApplicant)}|${extractOpt(ageCoApplicant)}|${extractOpt(applicantAgeGreaterThan62)}|${extractOpt(
        coapplicantAgeGreaterThan62)}|${extractOpt(applicationSubmission)}|" +
      s"${extractOpt(payable)}|${extractOpt(aus1)}|${extractOpt(aus2)}|${extractOpt(
        aus3)})}|${extractOpt(aus4)}|"
  }

}

case class ModifiedLarPartSix(aus5: Option[Int] = Some(0),
                              denialReason1: Option[Int] = Some(0),
                              denialReason2: Option[Int] = Some(0),
                              denialReason3: Option[Int] = Some(0),
                              denialReason4: Option[Int] = Some(0),
                              population: Option[String] = Some(""),
                              minorityPopulationPercent: Option[String] = Some(
                                ""),
                              ffiecMedFamIncome: Option[String] = Some(""),
                              medianIncomePercentage: Option[Int] = Some(0),
                              ownerOccupiedUnits: Option[String] = Some(""),
                              oneToFourFamUnits: Option[String] = Some(""),
                              medianAge: Option[Int] = Some(0))
    extends ColumnDataFormatter {

  def toPublicPSV: String = {
    s"${extractOpt(aus5)}|" +
      s"${extractOpt(denialReason1)}|${extractOpt(denialReason2)}|${extractOpt(
        denialReason3)}|${extractOpt(denialReason4)}|${extractOpt(population)}|" +
      s"${extractOpt(minorityPopulationPercent)}|${extractOpt(
        ffiecMedFamIncome)}|${extractOpt(medianIncomePercentage)}|" +
      s"${extractOpt(ownerOccupiedUnits)}|${extractOpt(oneToFourFamUnits)}|${extractOpt(medianAge)}"
  }

}

case class ModifiedLarPartSeven(tractToMsamd: Option[String] = Some(""),
                                medianAgeCalculated: Option[String] = Some(""),
                                percentMedianMsaIncome: Option[String] = Some(
                                  ""),
                                msaMDName: Option[String] = Some(""),
                                id: Int = 0,
                                uniqId: Int = 0,
                                createdAt: Timestamp)
    extends ColumnDataFormatter {

  def toPrivatePSV: String = {
    s"|${extractOpt(tractToMsamd)}|${extractOpt(medianAgeCalculated)}|${extractOpt(
      percentMedianMsaIncome)}|${extractOpt(msaMDName)}|$id"
  }
}

case class ModifiedLarEntityImpl(mlarPartOne: ModifiedLarPartOne,
                                 mlarPartTwo: ModifiedLarPartTwo,
                                 mlarPartThree: ModifiedLarPartThree,
                                 mlarPartFour: ModifiedLarPartFour,
                                 mlarPartFive: ModifiedLarPartFive,
                                 mlarPartSix: ModifiedLarPartSix,
                                 mlarPartSeven: ModifiedLarPartSeven) {

  def toPublicPSV: String =
    (mlarPartOne.toPublicPSV +
      mlarPartTwo.toPublicPSV +
      mlarPartThree.toPublicPSV +
      mlarPartFour.toPublicPSV +
      mlarPartFive.toPublicPSV +
      mlarPartSix.toPublicPSV).replaceAll("(\r\n)|\r|\n", "")

}
