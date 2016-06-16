package hmda.model.fi.lar

case class LoanApplicationRegister(
    id: Int,
    respondentId: String,
    agencyCode: Int,
    loan: Loan,
    preapprovals: Int,
    actionTakenType: Int,
    actionTakenDate: Int,
    geography: Geography,
    applicant: Applicant,
    purchaserType: Int,
    denial: Denial,
    rateSpread: String,
    hoepaStatus: Int,
    lienStatus: Int
) {
  def toCSV: String = {
    s"$id|$respondentId|$agencyCode|${loan.id}|${loan.applicationDate}" +
      s"|${loan.loanType}|${loan.propertyType}|${loan.purpose}|${loan.occupancy}" +
      s"|${loan.amount}|$preapprovals|$actionTakenType|$actionTakenDate" +
      s"|${geography.msa}|${geography.state}|${geography.county}|${geography.tract}" +
      s"|${applicant.ethnicity}|${applicant.coEthnicity}|${applicant.race1}" +
      s"|${applicant.race2}|${applicant.race3}|${applicant.race4}|${applicant.race5}" +
      s"|${applicant.coRace1}|${applicant.coRace2}|${applicant.coRace3}" +
      s"|${applicant.coRace4}|${applicant.coRace5}|${applicant.sex}|${applicant.coSex}" +
      s"|${applicant.income}|$purchaserType|${denial.reason1}|${denial.reason2}" +
      s"|${denial.reason3}|$rateSpread|$hoepaStatus|$lienStatus"
  }

  /**
   * NOTE:  The DAT file format is not supported by CFPB
   */
  def toDAT: String = {
    s"$id" +
      padLeftWithZero(respondentId, 10) + //Parser does not parse leading zeroes
      s"$agencyCode" +
      padRight(loan.id, 25) +
      padRight(loan.applicationDate, 8) +
      s"${loan.loanType}" +
      s"${loan.propertyType}" +
      s"${loan.purpose}" +
      s"${loan.occupancy}" +
      padLeftWithZero(loan.amount.toString, 5) +
      s"$preapprovals" +
      s"$actionTakenType" +
      s"$actionTakenDate" +
      padRight(geography.msa, 5) +
      s"${geography.state}" +
      padNumOrNa(geography.county, 3) +
      padNumOrNa(geography.tract, 7) +
      s"${applicant.ethnicity}" +
      s"${applicant.coEthnicity}" +
      s"${applicant.race1}" +
      padRight(applicant.race2, 1) +
      padRight(applicant.race3, 1) +
      padRight(applicant.race4, 1) +
      padRight(applicant.race5, 1) +
      s"${applicant.coRace1}" +
      padRight(applicant.coRace2, 1) +
      padRight(applicant.coRace3, 1) +
      padRight(applicant.coRace4, 1) +
      padRight(applicant.coRace5, 1) +
      s"${applicant.sex}" +
      s"${applicant.coSex}" +
      padLeftWithZero(applicant.income, 4) + //Parser does not parse leading zeroes
      s"$purchaserType" +
      padRight(denial.reason1, 1) +
      padRight(denial.reason2, 1) +
      padRight(denial.reason3, 1) +
      padNumOrNa(rateSpread, 5) +
      s"$hoepaStatus" +
      s"$lienStatus"
  }

  private def padRight(s: String, n: Int): String = {
    String.format("%1$-" + n + "s", s)
  }

  private def padLeftWithZero(s: String, n: Int): String = {
    String.format("%1$" + n + "s", s).replace(' ', '0')
  }

  private def padNumOrNa(s: String, n: Int): String = {
    if (s == "NA") {
      padRight(s, n)
    } else {
      padLeftWithZero(s, n)
    }
  }
}

