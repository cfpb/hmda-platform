package hmda.regulator.query.lar

case class LarEntity(
    id: Int = 0,
    lei: String = "",
    uli: String = "",
    appDate: String = "",
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
    tract: String = "",
    ethnicityApplicant1: Int = 0,
    ethnicityApplicant2: Int = 0,
    ethnicityApplicant3: Int = 0,
    ethnicityApplicant4: Int = 0,
    ethnicityApplicant5: Int = 0,
    otherHispanicApplicant: String = "",
    ethnicityCoApplicant1: Int = 0,
    ethnicityCoApplicant2: Int = 0,
    ethnicityCoApplicant3: Int = 0,
    ethnicityCoApplicant4: Int = 0,
    ethnicityCoApplicant5: Int = 0,
    otherHispanicCoApplicant: String = "",
    ethnicityObservedApplicant: Int = 0,
    ethnicityObservedCoApplicant: Int = 0,
    raceApplicant1: Int = 0,
    raceApplicant2: Int = 0,
    raceApplicant3: Int = 0,
    raceApplicant4: Int = 0,
    raceApplicant5: Int = 0,
    otherNativeRaceApplicant: String = "",
    otherAsianRaceApplicant: String = "",
    otherPacificRaceApplicant: String = "",
    rateCoApplicant1: Int = 0,
    rateCoApplicant2: Int = 0,
    rateCoApplicant3: Int = 0,
    rateCoApplicant4: Int = 0,
    rateCoApplicant5: Int = 0,
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
    income: String = "",
    purchaserType: Int = 0,
    rateSpread: String = "",
    hoepaStatus: Int = 0,
    lienStatus: Int = 0,
    creditScoreApplicant: Int = 0,
    creditScoreCoApplicant: Int = 0,
    creditScoreTypeApplicant: Int = 0,
    creditScoreModelApplicant: String = "",
    creditScoreTypeCoApplicant: Int = 0,
    creditScoreModelCoApplicant: String = "",
    denialReason1: Int = 0,
    denialReason2: Int = 0,
    denialReason3: Int = 0,
    denialReason4: Int = 0,
    otherDenialReason: String = "",
    totalLoanCosts: String = "",
    totalPoints: String = "",
    originationCharges: String = "",
    discountPoints: String = "",
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
    propertyValues: String = "",
    homeSecurityPolicy: Int = 0,
    landPropertyInterest: Int = 0,
    totalUnits: Int = 0,
    mfAffordable: String = "",
    applicationSubmission: Int = 0,
    payable: Int = 0,
    nmls: String = "",
    aus1: Int = 0,
    aus2: Int = 0,
    aus3: Int = 0,
    aus4: Int = 0,
    aus5: Int = 0,
    otheraus: String = "",
    aus1Result: Int = 0,
    aus2Result: Int = 0,
    aus3Result: Int = 0,
    aus4Result: Int = 0,
    aus5Result: Int = 0,
    otherAusResult: String = "",
    reverseMortgage: Int = 0,
    lineOfCredits: Int = 0,
    businessOrCommercial: Int = 0
) {
  def isEmpty: Boolean = lei == ""

  def toPSV: String = {
    s"$id|$lei|$uli|$appDate|$loanType|" +
      s"$loanPurpose|$preapproval|$constructionMethod|$occupancyType|" +
      s"$loanAmount|$actionTakenType|$actionTakenDate|$street|$city|$state|" +
      s"$zip|$county|$tract|$ethnicityApplicant1|$ethnicityApplicant2|$ethnicityApplicant3|" +
      s"$ethnicityApplicant4|$ethnicityApplicant5|$otherHispanicApplicant|$ethnicityCoApplicant1|" +
      s"$ethnicityCoApplicant2|$ethnicityCoApplicant3|$ethnicityCoApplicant4|$ethnicityCoApplicant5|" +
      s"$otherHispanicCoApplicant|$ethnicityObservedApplicant|$ethnicityObservedCoApplicant|$raceApplicant1" +
      s"|$raceApplicant2|$raceApplicant3|$raceApplicant4|$raceApplicant5|$otherNativeRaceApplicant|" +
      s"$otherAsianRaceApplicant|$otherPacificRaceApplicant|$rateCoApplicant1|$rateCoApplicant2|" +
      s"$rateCoApplicant3|$rateCoApplicant4|$rateCoApplicant5|$otherNativeRaceCoApplicant|" +
      s"$otherAsianRaceCoApplicant|$otherPacificRaceCoApplicant|$raceObservedApplicant|" +
      s"$raceObservedCoApplicant|$sexApplicant|$sexCoApplicant|$observedSexApplicant|$observedSexCoApplicant|" +
      s"$ageApplicant|$ageCoApplicant|$income|$purchaserType|$rateSpread|$hoepaStatus|$lienStatus|" +
      s"$creditScoreApplicant|$creditScoreCoApplicant|$creditScoreTypeApplicant|$creditScoreModelApplicant|" +
      s"$creditScoreTypeCoApplicant|$creditScoreModelCoApplicant|$denialReason1|$denialReason2|$denialReason3|" +
      s"$denialReason4|$otherDenialReason|$totalLoanCosts|$totalPoints|$originationCharges|" +
      s"$discountPoints|$lenderCredits|$interestRate|$paymentPenalty|$debtToIncome|$loanValueRatio|$loanTerm|" +
      s"$rateSpreadIntro|$baloonPayment|$insertOnlyPayment|$amortization|$otherAmortization|$propertyValues|" +
      s"$homeSecurityPolicy|$landPropertyInterest|$totalUnits|$mfAffordable|$applicationSubmission|$payable|" +
      s"$nmls|$aus1|$aus2|$aus3|$aus4|$aus5|$otheraus|$aus1Result|$aus2Result|$aus3Result|$aus4Result|$aus5Result|" +
      s"$otherAusResult|$reverseMortgage|$lineOfCredits|$businessOrCommercial"
  }
}
