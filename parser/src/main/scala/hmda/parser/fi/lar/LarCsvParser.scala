package hmda.parser.fi.lar

import hmda.model.fi.lar._

import scala.collection.immutable.ListMap
import scalaz._
import scalaz.Scalaz._
import scala.util.{ Failure, Success, Try }

object LarCsvParser {
  def apply(s: String): Either[List[String], LoanApplicationRegister] = {
    val values = s.split('|').map(_.trim)
    val larErrors = checkLar(values.toList)
    val validation = larErrors.disjunction
    if (validation.isRight) {
      val rSuccess = validation.toEither.right.get
      val id = rSuccess(0)
      val respId = values(1)
      val agencyCode = rSuccess(1)
      val loanId = values(3)
      val loanDate = values(4)
      val loanType = rSuccess(2)
      val propertyType = rSuccess(3)
      val loanPurpose = rSuccess(4)
      val occupancy = rSuccess(5)
      val loanAmount = rSuccess(6)
      val preapprovals = rSuccess(7)
      val actionType = rSuccess(8)
      val actionDate = rSuccess(9)
      val msa = values(13)
      val state = values(14)
      val county = values(15)
      val tract = values(16)
      val appEthnicity = rSuccess(10)
      val coAppEthnicity = rSuccess(11)
      val appRace1 = rSuccess(12)
      val appRace2 = values(20)
      val appRace3 = values(21)
      val appRace4 = values(22)
      val appRace5 = values(23)
      val coAppRace1 = rSuccess(13)
      val coAppRace2 = values(25)
      val coAppRace3 = values(26)
      val coAppRace4 = values(27)
      val coAppRace5 = values(28)
      val appSex = rSuccess(14)
      val coAppSex = rSuccess(15)
      val appIncome = values(31)
      val purchaserType = rSuccess(16)
      val denial1 = values(33)
      val denial2 = values(34)
      val denial3 = values(35)
      val rateSpread = values(36)
      val hoepaStatus = rSuccess(17)
      val lienStatus = rSuccess(18)

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
    } else {
      val lErrors = validation.toEither.left.get
      Left(lErrors.list.toList)
    }
  }

  def toIntorFail(value: String, fieldName: String): ValidationNel[String, List[Int]] = {
    Try(value.toInt) match {
      case Failure(result) => s"$fieldName is not an Integer".failure.toValidationNel
      case Success(result) => List(result).success
    }
  }

  def checkLar(fields: List[String]): ValidationNel[String, List[Int]] = {

    if (fields.length != 40 && fields.length != 39) {
      ("Incorrect number of fields: " + fields.length).failure.toValidationNel
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

      val validationList = numericFields.map { case (key, value) => toIntorFail(value, key) }

      validationList.reduce(_ +++ _)
    }
  }
}
