package hmda.parser.fi

import hmda.model.fi.lar._

object LoanApplicationRegisterDATReader {
  def apply(s: String): LoanApplicationRegister = {
    val id = s.substring(0, 1).toInt
    val respId = s.substring(1, 11)
    val code = s.substring(11, 12).toInt
    val loanId = s.substring(12, 37)
    val loanDate = s.substring(37, 45)
    val loanType = s.substring(45, 46).toInt
    val propertyType = s.substring(46, 47).toInt
    val loanPurpose = s.substring(47, 48).toInt
    val occupancy = s.substring(48, 49).toInt
    val loanAmount = s.substring(49, 54).toInt
    val preapprovals = s.substring(54, 55).toInt
    val actionType = s.substring(55, 56).toInt
    val actionDate = s.substring(56, 64).toInt
    val msa = s.substring(64, 69)
    val state = s.substring(69, 71)
    val county = s.substring(71, 74)
    val tract = s.substring(74, 81)
    val appEthnicity = s.substring(81, 82).toInt
    val coAppEthnicity = s.substring(82, 83).toInt
    val appRace1 = s.substring(83, 84).toInt
    val appRace2 = s.substring(84, 85)
    val appRace3 = s.substring(85, 86)
    val appRace4 = s.substring(86, 87)
    val appRace5 = s.substring(87, 88)
    val coAppRace1 = s.substring(88, 89).toInt
    val coAppRace2 = s.substring(89, 90)
    val coAppRace3 = s.substring(90, 91)
    val coAppRace4 = s.substring(91, 92)
    val coAppRace5 = s.substring(92, 93)
    val appSex = s.substring(93, 94).toInt
    val coAppSex = s.substring(94, 95).toInt
    val appIncome = s.substring(95, 99)
    val purchaserType = s.substring(99, 100).toInt
    val denial1 = s.substring(100, 101)
    val denial2 = s.substring(101, 102)
    val denial3 = s.substring(102, 103)
    val rateSpread = s.substring(103, 108)
    val hoepaStatus = s.substring(108, 109).toInt
    val lienStatus = s.substring(109, 110).toInt

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
      code,
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
