package hmda.validation.engine.lar.validity

import java.io.File
import hmda.model.fi.lar._
import hmda.parser.fi.lar.LarCsvParser
import org.scalatest.{ MustMatchers, WordSpec }

import scala.io.Source
import scalaz.IList

class LarValidityEngineSpec extends WordSpec with MustMatchers with LarValidityEngine {

  "LAR Validity engine" must {
    "pass validation on valid sample file" in {
      val lines = Source.fromFile(new File("parser/src/test/resources/txt/FirstTestBankData_clean_407_2017.txt")).getLines()
      val lars = lines.drop(1).map(l => LarCsvParser(l))

      lars.foreach { lar =>
        checkValidity(lar) mustBe a[scalaz.Success[_]]
      }
    }

    "pass for validLar1" in {
      checkValidity(validLar1) mustBe a[scalaz.Success[_]]
    }

    "fail validation for LAR that fails V210" in {
      val lar = validLar1.copy(loan = validLoan.copy(applicationDate = "19970309"))
      errors(lar) mustBe List("V210")
    }
  }

  def errors(lar: LoanApplicationRegister): List[String] = {
    checkValidity(lar) match {
      case scalaz.Failure(x) => x.map(f => f.msg).list.toList
      case scalaz.Success(_) => IList("no errors").toList
    }
  }

  // Sample data
  val validLoan = Loan(
    id = "2009234466",
    applicationDate = "20170309",
    loanType = 1,
    propertyType = 1,
    purpose = 2,
    occupancy = 1,
    amount = 25
  )
  val validGeography = Geography(
    msa = "22500",
    state = "45",
    county = "041",
    tract = "0019.00"
  )
  val validApplicant = Applicant(
    ethnicity = 2,
    coEthnicity = 2,
    race1 = 5,
    race2 = "",
    race3 = "",
    race4 = "",
    race5 = "",
    coRace1 = 5,
    coRace2 = "",
    coRace3 = "",
    coRace4 = "",
    coRace5 = "",
    sex = 1,
    coSex = 2,
    income = "103"
  )
  val validDenial = Denial(
    reason1 = "",
    reason2 = "",
    reason3 = ""
  )

  // Sample LAR is the same as line 8 of
  //   parser/src/test/resources/txt/FirstTestBankData_clean_407_2017.txt
  val validLar1 = LoanApplicationRegister(
    id = 2,
    respondentId = "8800009923",
    agencyCode = 3,
    loan = validLoan,
    preapprovals = 3,
    actionTakenType = 4,
    actionTakenDate = 20170315,
    geography = validGeography,
    applicant = validApplicant,
    purchaserType = 0,
    denial = validDenial,
    rateSpread = "NA",
    hoepaStatus = 2,
    lienStatus = 2
  )

}
