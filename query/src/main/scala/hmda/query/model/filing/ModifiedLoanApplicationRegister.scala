package hmda.query.model.filing

case class ModifiedLoanApplicationRegister(
    id: Int = 2,
    respondentId: String = "",
    agencyCode: Int = 0,
    preapprovals: Int = 0,
    actionTakenType: Int = 0,
    purchaserType: Int = 0,
    rateSpread: String = "",
    hoepaStatus: Int = 0,
    lienStatus: Int = 0,
    loanType: Int = 0,
    propertyType: Int = 0,
    purpose: Int = 0,
    occupancy: Int = 0,
    amount: Int = 0,
    msa: String = "NA",
    state: String = "NA",
    county: String = "NA",
    tract: String = "NA",
    ethnicity: Int = 0,
    coEthnicity: Int = 0,
    race1: Int = 0,
    race2: String = "",
    race3: String = "",
    race4: String = "",
    race5: String = "",
    coRace1: Int = 0,
    coRace2: String = "",
    coRace3: String = "",
    coRace4: String = "",
    coRace5: String = "",
    sex: Int = 0,
    coSex: Int = 0,
    income: String = "",
    denialReason1: String = "",
    denialReason2: String = "",
    denialReason3: String = "",
    period: String = ""
) {
  def toCSV: String = {
    s"$id|$respondentId|$agencyCode" +
      s"|$loanType|$propertyType|$purpose|$occupancy" +
      s"|$amount|$preapprovals|$actionTakenType" +
      s"|$msa|$state|$county|$tract" +
      s"|$ethnicity|$coEthnicity|$race1" +
      s"|$race2|$race3|$race4|$race5" +
      s"|$coRace1|$coRace2|$coRace3" +
      s"|$coRace4|$coRace5|$sex|$coSex" +
      s"|$income|$purchaserType|$denialReason1|$denialReason2" +
      s"|$denialReason3|$rateSpread|$hoepaStatus|$lienStatus"
  }
}
