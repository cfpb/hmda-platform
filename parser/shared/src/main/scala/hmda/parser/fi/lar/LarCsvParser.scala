package hmda.parser.fi.lar

import hmda.model.fi.lar._

import scala.collection.immutable.ListMap
import scalaz._
import scalaz.Scalaz._
import scala.util.{ Failure, Success, Try }

object LarCsvParser {
  def apply(s: String): Either[LarParsingError, LoanApplicationRegister] = {
    apply(s, 0)
  }

  def apply(s: String, i: Int): Either[LarParsingError, LoanApplicationRegister] = {
    val values = (s + " ").split('|').map(_.trim)
    val parserResults = checkLar(values.toList)
    parserResults match {
      case scalaz.Success(convertedValues) => {

        val id = convertedValues(0)
        val respId = values(1)
        val agencyCode = convertedValues(1)
        val loanId = values(3)
        val loanDate = values(4)
        val loanType = convertedValues(2)
        val propertyType = convertedValues(3)
        val loanPurpose = convertedValues(4)
        val occupancy = convertedValues(5)
        val loanAmount = convertedValues(6)
        val preapprovals = convertedValues(7)
        val actionType = convertedValues(8)
        val actionDate = convertedValues(9)
        val msa = values(13)
        val state = values(14)
        val county = values(15)
        val tract = values(16)
        val appEthnicity = convertedValues(10)
        val coAppEthnicity = convertedValues(11)
        val appRace1 = convertedValues(12)
        val appRace2 = values(20)
        val appRace3 = values(21)
        val appRace4 = values(22)
        val appRace5 = values(23)
        val coAppRace1 = convertedValues(13)
        val coAppRace2 = values(25)
        val coAppRace3 = values(26)
        val coAppRace4 = values(27)

        val coAppRace5 = values(28)
        val appSex = convertedValues(14)
        val coAppSex = convertedValues(15)
        val appIncome = values(31)
        val purchaserType = convertedValues(16)
        val denial1 = values(33)
        val denial2 = values(34)
        val denial3 = values(35)
        val rateSpread = values(36)
        val hoepaStatus = convertedValues(17)
        val lienStatus = convertedValues(18)

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

        Right(
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
        )
      }
      case scalaz.Failure(errors) => {
        Left(LarParsingError(i, errors.toList))
      }
    }
  }

  def checkLar(fields: List[String]): ValidationNel[String, List[Int]] = {

    if (fields.length != 39) {
      s"An incorrect number of data fields were reported: ${fields.length} data fields were found, when 39 data fields were expected.".failure.toValidationNel
    } else {
      val numericFields = ListMap(
        "Record Identifier" -> fields(0),
        "Agency Code" -> fields(2),
        "Loan Type" -> fields(5),
        "Property Type" -> fields(6),
        "Loan Purpose" -> fields(7),
        "Owner Occupancy" -> fields(8),
        "Loan Amount" -> fields(9),
        "Preapprovals" -> fields(10),
        "Type of Action Taken" -> fields(11),
        "Date of Action" -> fields(12),
        "Applicant Ethnicity" -> fields(17),
        "Co-applicant Ethnicity" -> fields(18),
        "Applicant Race: 1" -> fields(19),
        "Co-applicant Race: 1" -> fields(24),
        "Applicant Sex" -> fields(29),
        "Co-applicant Sex" -> fields(30),
        "Type of Purchaser" -> fields(32),
        "HOEPA Status" -> fields(37),
        "Lien Status" -> fields(38)
      )

      val validationList = numericFields.map { case (key, value) => toIntOrFail(value, key) }

      validationList.reduce(_ +++ _)
    }
  }

  def toIntOrFail(value: String, fieldName: String): ValidationNel[String, List[Int]] = {
    Try(value.toInt) match {
      case Failure(result) => s"$fieldName is not an integer".failure.toValidationNel
      case Success(result) => List(result).success
    }
  }
}
