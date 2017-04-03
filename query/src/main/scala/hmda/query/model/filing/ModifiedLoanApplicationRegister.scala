package hmda.query.model.filing

case class ModifiedLoanApplicationRegister(
    id: String,
    respondentId: String,
    agencyCode: Int,
    preapprovals: Int,
    actionTakenType: Int,
    purchaserType: Int,
    rateSpread: String,
    hoepaStatus: Int,
    lienStatus: Int,
    loanType: Int,
    propertyType: Int,
    purpose: Int,
    occupancy: Int,
    amount: Int,
    msa: String,
    state: String,
    county: String,
    tract: String,
    ethnicity: Int,
    coEthnicity: Int,
    race1: Int,
    race2: String,
    race3: String,
    race4: String,
    race5: String,
    coRace1: Int,
    coRace2: String,
    coRace3: String,
    coRace4: String,
    coRace5: String,
    sex: Int,
    coSex: Int,
    income: String,
    denialReason1: String,
    denialReason2: String,
    denialReason3: String,
    institutionId: String,
    period: String
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
