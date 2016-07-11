package hmda.parser.fi.lar

import hmda.model.fi.lar._

import scala.util.Try

object LarCsvParser {
  def apply(s: String): Either[List[String], LoanApplicationRegister] = {
    val larErrors = checkLar(s)

    if (larErrors.isEmpty) {
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

      Right(LoanApplicationRegister(
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
      ))
    } else {
      Left(larErrors)
    }
  }

  def checkLar(s: String): List[String] = {
    val values = s.split('|').map(_.trim)
    var errors = List[String]()
    val numericFields = Map(
      0 -> "Record Identifier",
      2 -> "Agency Code",
      5 -> "Loan Type",
      6 -> "Property Type",
      7 -> "Loan Purpose",
      8 -> "Owner Occupancy",
      9 -> "Loan Amount",
      10 -> "Preapprovals",
      11 -> "Type of Action Taken",
      12 -> "Date of Action",
      17 -> "Applicant Ethnicity",
      18 -> "Co-applicant Ethnicity",
      19 -> "Applicant Race: 1",
      24 -> "Co-applicant Race: 1",
      29 -> "Applicant Sex",
      30 -> "Co-applicant Sex",
      32 -> "Type of Purchaser",
      37 -> "HOEPA Status",
      38 -> "Lien Status"
    )

    if (values.length != 40 && values.length != 39) {
      return errors ::: List("Incorrect number of fields: " + values.length)
    }

    for (int <- numericFields.keys) {
      if (Try(values(int).toInt).isFailure) {
        val field = numericFields(int)
        errors = errors ::: List(s"$field is not an Integer")
      }
    }

    errors
  }
}
