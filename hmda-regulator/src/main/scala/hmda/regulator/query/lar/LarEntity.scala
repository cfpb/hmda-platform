package hmda.regulator.query.lar

import hmda.util.conversion.ColumnDataFormatter

case class LarPartOne(id: Int = 0,
                      lei: String = "",
                      uli: String = "",
                      applicationDate: String = "",
                      loanType: Int = 0,
                      loanPurpose: Int = 0,
                      preapproval: Int = 0,
                      constructionMethod: Int = 0,
                      occupancyType: Int = 0,
                      loanAmount: Double = 0.0,
                      actionTakenType: Int = 0,
                      actionTakenDate: Int = 0,
                      street: String = "",
                      city: String = "",
                      state: String = "",
                      zip: String = "",
                      county: String = "",
                      tract: String = "") {
  def isEmpty: Boolean = lei == ""

  def toPSV: String = {
    s"$id|$lei|$uli|$applicationDate|$loanType|" +
      s"$loanPurpose|$preapproval|$constructionMethod|$occupancyType|" +
      BigDecimal.valueOf(loanAmount).toString() +
      s"|$actionTakenType|$actionTakenDate|$street|$city|$state|" +
      s"$zip|$county|$tract|"
  }
}
case class LarPartTwo(ethnicityApplicant1: String = "",
                      ethnicityApplicant2: String = "",
                      ethnicityApplicant3: String = "",
                      ethnicityApplicant4: String = "",
                      ethnicityApplicant5: String = "",
                      otherHispanicApplicant: String = "",
                      ethnicityCoApplicant1: String = "",
                      ethnicityCoApplicant2: String = "",
                      ethnicityCoApplicant3: String = "",
                      ethnicityCoApplicant4: String = "",
                      ethnicityCoApplicant5: String = "",
                      otherHispanicCoApplicant: String = "",
                      ethnicityObservedApplicant: Int = 0,
                      ethnicityObservedCoApplicant: Int = 0,
                      raceApplicant1: String = "",
                      raceApplicant2: String = "",
                      raceApplicant3: String = "",
                      raceApplicant4: String = "",
                      raceApplicant5: String = "") {

  def toPSV: String = {
    s"$ethnicityApplicant1|$ethnicityApplicant2|$ethnicityApplicant3|" +
      s"$ethnicityApplicant4|$ethnicityApplicant5|$otherHispanicApplicant|$ethnicityCoApplicant1|" +
      s"$ethnicityCoApplicant2|$ethnicityCoApplicant3|$ethnicityCoApplicant4|$ethnicityCoApplicant5|" +
      s"$otherHispanicCoApplicant|$ethnicityObservedApplicant|$ethnicityObservedCoApplicant|$raceApplicant1" +
      s"|$raceApplicant2|$raceApplicant3|$raceApplicant4|$raceApplicant5|"
  }
}

case class LarPartThree(otherNativeRaceApplicant: String = "",
                        otherAsianRaceApplicant: String = "",
                        otherPacificRaceApplicant: String = "",
                        raceCoApplicant1: String = "",
                        raceCoApplicant2: String = "",
                        raceCoApplicant3: String = "",
                        raceCoApplicant4: String = "",
                        raceCoApplicant5: String = "",
                        otherNativeRaceCoApplicant: String = "",
                        otherAsianRaceCoApplicant: String = "",
                        otherPacificRaceCoApplicant: String = "",
                        raceObservedApplicant: Int = 0,
                        raceObservedCoApplicant: Int = 0,
                        sexApplicant: Int = 0,
                        sexCoApplicant: Int = 0,
                        observedSexApplicant: Int = 0,
                        observedSexCoApplicant: Int = 0,
                        ageApplicant: Int = 0,
                        ageCoApplicant: Int = 0,
                        income: String = "") {

  def toPSV: String = {
    s"$otherNativeRaceApplicant|" +
      s"$otherAsianRaceApplicant|$otherPacificRaceApplicant|$raceCoApplicant1|$raceCoApplicant2|" +
      s"$raceCoApplicant3|$raceCoApplicant4|$raceCoApplicant5|$otherNativeRaceCoApplicant|" +
      s"$otherAsianRaceCoApplicant|$otherPacificRaceCoApplicant|$raceObservedApplicant|" +
      s"$raceObservedCoApplicant|$sexApplicant|$sexCoApplicant|$observedSexApplicant|$observedSexCoApplicant|" +
      s"$ageApplicant|$ageCoApplicant|$income|"
  }
}

case class LarPartFour(purchaserType: Int = 0,
                       rateSpread: String = "",
                       hoepaStatus: Int = 0,
                       lienStatus: Int = 0,
                       creditScoreApplicant: Int = 0,
                       creditScoreCoApplicant: Int = 0,
                       creditScoreTypeApplicant: Int = 0,
                       creditScoreModelApplicant: String = "",
                       creditScoreTypeCoApplicant: Int = 0,
                       creditScoreModelCoApplicant: String = "",
                       denialReason1: String = "",
                       denialReason2: String = "",
                       denialReason3: String = "",
                       denialReason4: String = "",
                       otherDenialReason: String = "",
                       totalLoanCosts: String = "",
                       totalPoints: String = "",
                       originationCharges: String = "") {

  def toPSV: String = {
    s"$purchaserType|$rateSpread|$hoepaStatus|$lienStatus|" +
      s"$creditScoreApplicant|$creditScoreCoApplicant|$creditScoreTypeApplicant|$creditScoreModelApplicant|" +
      s"$creditScoreTypeCoApplicant|$creditScoreModelCoApplicant|$denialReason1|$denialReason2|$denialReason3|" +
      s"$denialReason4|$otherDenialReason|$totalLoanCosts|$totalPoints|$originationCharges|"
  }
}

case class LarPartFive(discountPoints: String = "",
                       lenderCredits: String = "",
                       interestRate: String = "",
                       paymentPenalty: String = "",
                       debtToIncome: String = "",
                       loanValueRatio: String = "",
                       loanTerm: String = "",
                       rateSpreadIntro: String = "",
                       baloonPayment: Int = 0,
                       insertOnlyPayment: Int = 0,
                       amortization: Int = 0,
                       otherAmortization: Int = 0,
                       propertyValue: String = "",
                       homeSecurityPolicy: Int = 0,
                       landPropertyInterest: Int = 0,
                       totalUnits: Int = 0,
                       mfAffordable: String = "",
                       applicationSubmission: Int = 0) {

  def toPSV: String = {
    s"$discountPoints|$lenderCredits|$interestRate|$paymentPenalty|$debtToIncome|$loanValueRatio|$loanTerm|" +
      s"$rateSpreadIntro|$baloonPayment|$insertOnlyPayment|$amortization|$otherAmortization|" +
      ColumnDataFormatter.toBigDecimalString(propertyValue) + "|" +
      s"$homeSecurityPolicy|$landPropertyInterest|$totalUnits|$mfAffordable|$applicationSubmission|"
  }

}

case class LarPartSix(payable: Int = 0,
                      nmls: String = "",
                      aus1: String = "",
                      aus2: String = "",
                      aus3: String = "",
                      aus4: String = "",
                      aus5: String = "",
                      otheraus: String = "",
                      aus1Result: String = "",
                      aus2Result: String = "",
                      aus3Result: String = "",
                      aus4Result: String = "",
                      aus5Result: String = "",
                      otherAusResult: String = "",
                      reverseMortgage: Int = 0,
                      lineOfCredits: Int = 0,
                      businessOrCommercial: Int = 0) {

  def toPSV: String = {
    s"$payable|" +
      s"$nmls|$aus1|$aus2|$aus3|$aus4|$aus5|$otheraus|$aus1Result|$aus2Result|$aus3Result|$aus4Result|$aus5Result|" +
      s"$otherAusResult|$reverseMortgage|$lineOfCredits|$businessOrCommercial"
  }
}

case class LarEntityImpl(larPartOne: LarPartOne,
                         larPartTwo: LarPartTwo,
                         larPartThree: LarPartThree,
                         larPartFour: LarPartFour,
                         larPartFive: LarPartFive,
                         larPartSix: LarPartSix) {

  def toPSV: String =
    larPartOne.toPSV +
      larPartTwo.toPSV +
      larPartThree.toPSV +
      larPartFour.toPSV +
      larPartFive.toPSV +
      larPartSix.toPSV

}
