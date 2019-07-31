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
) {
  def isEmpty: Boolean = lei == ""

  def toPublicPSV: String = {
    s"$filingYear|$lei|$msaMd|$state|$county|" +
      s"$tract|$conformingLoanLimit|$loanFlag|$ethnicityCategorization|" +
      s"|$raceCategorization|$sexCategorization|$actionTakenType|$purchaserType|" +
      s"$preapproval|$loanType|$loanPurpose|$lienStatus|"
  }

  def toPublicCSV: String = {
    s"$filingYear,$lei,$msaMd,$state,$county," +
      s"$tract,$conformingLoanLimit,$loanFlag,$ethnicityCategorization," +
      s",$raceCategorization,$sexCategorization,$actionTakenType,$purchaserType," +
      s"$preapproval,$loanType,$loanPurpose,$lienStatus,"
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
                              insertOnlyPayment: Option[Int] = Some(0)) {

  def toPublicPSV: String = {
    s"$reverseMortgage|$lineOfCredits|$businessOrCommercial|" +
      BigDecimal.valueOf(loanAmount).bigDecimal.toPlainString +
      s"|$loanValueRatio|$interestRate|$rateSpread|$hoepaStatus|" +
      s"$totalLoanCosts|$totalPoints|$originationCharges|$discountPoints|" +
      s"$lenderCredits|$loanTerm|$paymentPenalty|$rateSpreadIntro" +
      s"|$amortization|$insertOnlyPayment|"
  }

  def toPublicCSV: String = {
    s"$reverseMortgage,$lineOfCredits,$businessOrCommercial," +
      BigDecimal.valueOf(loanAmount).bigDecimal.toPlainString +
      s",$loanValueRatio,$interestRate,$rateSpread,$hoepaStatus," +
      s"$totalLoanCosts,$totalPoints,$originationCharges,$discountPoints," +
      s"$lenderCredits,$loanTerm,$paymentPenalty,$rateSpreadIntro" +
      s",$amortization,$insertOnlyPayment,"
  }

}

case class ModifiedLarPartThree(
    baloonPayment: Option[Int] = Some(0),
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
    ethnicityApplicant4: Option[String] = Some("")) {

  def toPublicPSV: String = {
    s"$baloonPayment|" +
      s"$otherAmortization|" +
      ColumnDataFormatter.toBigDecimalString(propertyValue) + "|" +
      s"$constructionMethod|$occupancyType|" +
      s"$homeSecurityPolicy|$landPropertyInterest|$totalUnits|$mfAffordable|" +
      s"$income|$debtToIncome|$creditScoreTypeApplicant|" +
      s"$creditScoreTypeCoApplicant|$ethnicityApplicant1|$ethnicityApplicant2|$ethnicityApplicant3|$ethnicityApplicant4|"
  }

  def toPublicCSV: String = {
    s"$baloonPayment," +
      s"$otherAmortization,$propertyValue,$constructionMethod,$occupancyType," +
      s"$homeSecurityPolicy,$landPropertyInterest,$totalUnits,$mfAffordable," +
      s"$income,$debtToIncome,$creditScoreTypeApplicant," +
      s"$creditScoreTypeCoApplicant,$ethnicityApplicant1,$ethnicityApplicant2,$ethnicityApplicant3,$ethnicityApplicant4,"
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
    raceCoApplicant4: Option[String] = Some("")) {

  def toPublicPSV: String = {
    s"$ethnicityApplicant5|$ethnicityCoApplicant1|$ethnicityCoApplicant2|$ethnicityCoApplicant3|" +
      s"$ethnicityCoApplicant4|$ethnicityCoApplicant5|$ethnicityObservedApplicant|$ethnicityObservedCoApplicant|" +
      s"$raceApplicant1|$raceApplicant2|" +
      s"$raceApplicant3|$raceApplicant4|$raceApplicant5|$raceCoApplicant1|" +
      s"$raceCoApplicant2|$raceCoApplicant3|$raceCoApplicant4|"
  }

  def toPublicCSV: String = {
    s"$ethnicityApplicant5,$ethnicityCoApplicant1,$ethnicityCoApplicant2,$ethnicityCoApplicant3," +
      s"$ethnicityCoApplicant4,$ethnicityCoApplicant5,$ethnicityObservedApplicant,$ethnicityObservedCoApplicant," +
      s"$raceApplicant1,$raceApplicant2," +
      s"$raceApplicant3,$raceApplicant4,$raceApplicant5,$raceCoApplicant1," +
      s"$raceCoApplicant2,$raceCoApplicant3,$raceCoApplicant4,"
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
    aus4: Option[Int] = Some(0)) {

  def toPublicPSV: String = {
    s"$raceCoApplicant5|$raceObservedApplicant|$raceObservedCoApplicant|$sexApplicant|$sexCoApplicant|$observedSexApplicant|$observedSexCoApplicant|" +
      s"$ageApplicant|$ageCoApplicant|$applicantAgeGreaterThan62|$coapplicantAgeGreaterThan62|$applicationSubmission|" +
      s"$payable|$aus1|$aus2|$aus3|$aus4|"
  }

  def toPublicCSV: String = {
    s"$raceCoApplicant5,$raceObservedApplicant,$raceObservedCoApplicant,$sexApplicant,$sexCoApplicant,$observedSexApplicant,$observedSexCoApplicant," +
      s"$ageApplicant,$ageCoApplicant,$applicantAgeGreaterThan62,$coapplicantAgeGreaterThan62,$applicationSubmission," +
      s"$payable,$aus1,$aus2,$aus3,$aus4,"
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
                              medianAge: Option[Int] = Some(0)) {

  def toPublicPSV: String = {
    s"$aus5|" +
      s"$denialReason1|$denialReason2|$denialReason3|$denialReason4|$population|" +
      s"$aus5|$minorityPopulationPercent|$ffiecMedFamIncome|$medianIncomePercentage|" +
      s"$ownerOccupiedUnits|$oneToFourFamUnits|$medianAge"
  }

  def toPublicCSV: String = {
    s"$aus5," +
      s"$denialReason1,$denialReason2,$denialReason3,$denialReason4,$population," +
      s"$aus5,$minorityPopulationPercent,$ffiecMedFamIncome,$medianIncomePercentage," +
      s"$ownerOccupiedUnits,$oneToFourFamUnits,$medianAge"
  }
}

case class ModifiedLarPartSeven(tractToMsamd: Option[String] = Some(""),
                                medianAgeCalculated: Option[String] = Some(""),
                                percentMedianMsaIncome: Option[String] = Some(
                                  ""),
                                msaMDName: Option[String] = Some(""),
                                id: Int = 0,
                                uniqId: Int = 0,
                                createdAt: Timestamp) {

  def toPrivatePSV: String = {
    s"|$tractToMsamd|$medianAgeCalculated|$percentMedianMsaIncome|$msaMDName|$id"
  }
}

case class ModifiedLarEntityImpl(mlarPartOne: ModifiedLarPartOne,
                                 mlarPartTwo: ModifiedLarPartTwo,
                                 mlarPartThree: ModifiedLarPartThree,
                                 mlarPartFour: ModifiedLarPartFour,
                                 mlarPartFive: ModifiedLarPartFive,
                                 mlarPartSix: ModifiedLarPartSix,
                                 mlarPartSeven: ModifiedLarPartSeven) {

  def toPrivatePSV: String =
    (mlarPartOne.toPublicPSV +
      mlarPartTwo.toPublicPSV +
      mlarPartThree.toPublicPSV +
      mlarPartFour.toPublicPSV +
      mlarPartFive.toPublicPSV +
      mlarPartSix.toPublicPSV +
      mlarPartSeven.toPrivatePSV).replaceAll("(\r\n)|\r|\n", "")

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
}
