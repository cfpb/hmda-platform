package hmda.parser.fi.lar

import hmda.model.fi.lar._

object LarDatParser {
  def apply(s: String): LoanApplicationRegister = {
    val id = s.substring(0, 1).trim.toInt
    val respId = s.substring(1, 11).trim
    val code = s.substring(11, 12).trim.toInt
    val loanId = s.substring(12, 37).trim
    val loanDate = s.substring(37, 45).trim
    val loanType = s.substring(45, 46).trim.toInt
    val propertyType = s.substring(46, 47).trim.toInt
    val loanPurpose = s.substring(47, 48).trim.toInt
    val occupancy = s.substring(48, 49).trim.toInt
    val loanAmount = s.substring(49, 54).trim.toInt
    val preapprovals = s.substring(54, 55).trim.toInt
    val actionType = s.substring(55, 56).trim.toInt
    val actionDate = s.substring(56, 64).trim.toInt
    val msa = s.substring(64, 69).trim
    val state = s.substring(69, 71).trim
    val county = s.substring(71, 74).trim
    val tract = s.substring(74, 81).trim
    val appEthnicity = s.substring(81, 82).trim.toInt
    val coAppEthnicity = s.substring(82, 83).trim.toInt
    val appRace1 = s.substring(83, 84).trim.toInt
    val appRace2 = s.substring(84, 85).trim
    val appRace3 = s.substring(85, 86).trim
    val appRace4 = s.substring(86, 87).trim
    val appRace5 = s.substring(87, 88).trim
    val coAppRace1 = s.substring(88, 89).trim.toInt
    val coAppRace2 = s.substring(89, 90).trim
    val coAppRace3 = s.substring(90, 91).trim
    val coAppRace4 = s.substring(91, 92).trim
    val coAppRace5 = s.substring(92, 93).trim
    val appSex = s.substring(93, 94).trim.toInt
    val coAppSex = s.substring(94, 95).trim.toInt
    val appIncome = s.substring(95, 99).trim.replaceFirst("^0+(?!$)", "")
    val purchaserType = s.substring(99, 100).trim.toInt
    val denial1 = s.substring(100, 101).trim
    val denial2 = s.substring(101, 102).trim
    val denial3 = s.substring(102, 103).trim
    val rateSpread = s.substring(103, 108).trim
    val hoepaStatus = s.substring(108, 109).trim.toInt
    val lienStatus = s.substring(109, 110).trim.toInt

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
