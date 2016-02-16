package hmda.parser.fi.lar

import hmda.model.fi.lar._

object LarCsvParser {
  def apply(s: String): LoanApplicationRegister = {
    val values = s.split('|').map(_.trim)
    val id = values(0).toInt
    val respId = values(1)
    val agencyCode = values(2).toInt
    val loanId = values(3)
    val loanDate = values(4)
    val loanType = values(5).toInt
    val propertyType = values(6).toInt
    val loanPurpose = values(7).toInt
    val occupancy = values(8).toInt
    val loanAmount = values(9).toInt
    val preapprovals = values(10).toInt
    val actionType = values(11).toInt
    val actionDate = values(12).toInt
    val msa = values(13)
    val state = values(14)
    val county = values(15)
    val tract = values(16)
    val appEthnicity = values(17).toInt
    val coAppEthnicity = values(18).toInt
    val appRace1 = values(19).toInt
    val appRace2 = values(20)
    val appRace3 = values(21)
    val appRace4 = values(22)
    val appRace5 = values(23)
    val coAppRace1 = values(24).toInt
    val coAppRace2 = values(25)
    val coAppRace3 = values(26)
    val coAppRace4 = values(27)
    val coAppRace5 = values(28)
    val appSex = values(29).toInt
    val coAppSex = values(30).toInt
    val appIncome = values(31)
    val purchaserType = values(32).toInt
    val denial1 = values(33)
    val denial2 = values(34)
    val denial3 = values(35)
    val rateSpread = values(36)
    val hoepaStatus = values(37).toInt
    val lienStatus = values(38).toInt

    val loan =
      Loan(
        loanId,
        loanDate,
        loanType,
        propertyType,
        loanPurpose,
        occupancy,
        loanAmount
      )

    val geography = Geography(msa, state, county, tract)

    val applicant =
      Applicant(
        appEthnicity,
        coAppEthnicity,
        appRace1,
        appRace2,
        appRace3,
        appRace4,
        appRace5,
        coAppRace1,
        coAppRace2,
        coAppRace3,
        coAppRace4,
        coAppRace5,
        appSex,
        coAppSex,
        appIncome
      )
    val denial = Denial(denial1, denial2, denial3)

    LoanApplicationRegister(
      id,
      respId,
      agencyCode,
      loan,
      preapprovals,
      actionType,
      actionDate,
      geography,
      applicant,
      purchaserType,
      denial,
      rateSpread,
      hoepaStatus,
      lienStatus
    )
  }
}
