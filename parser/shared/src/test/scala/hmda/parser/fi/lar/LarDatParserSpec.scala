package hmda.parser.fi.lar

import hmda.model.fi.lar.{ Loan, Geography, Denial, Applicant }
import hmda.parser.util.FITestData
import org.scalatest.{ FlatSpec, MustMatchers }

import scala.concurrent.{ ExecutionContext, Future }

class LarDatParserSpec extends FlatSpec with MustMatchers {

  import FITestData._

  val lars = larsDAT.map(line => LarDatParser(line))
  val firstLar = lars.head

  "LAR Parser" should "parse correct number of LARs" in {
    lars.size mustBe 3
  }

  it should "parse basic LAR information" in {
    firstLar.id mustBe 2
    firstLar.respondentId mustBe "0123456789"
    firstLar.agencyCode mustBe 9
    firstLar.preapprovals mustBe 1
    firstLar.actionTakenType mustBe 5
    firstLar.actionTakenDate mustBe 20130119
    firstLar.purchaserType mustBe 0
    firstLar.rateSpread mustBe "01.05"
    firstLar.hoepaStatus mustBe 2
    firstLar.lienStatus mustBe 4
  }

  it should "parse loan information" in {
    val loan = firstLar.loan

    loan mustBe a[Loan]

    loan.id mustBe "ABCDEFGHIJKLMNOPQRSTUVWXY"
    loan.applicationDate mustBe "20130117"
    loan.loanType mustBe 4
    loan.propertyType mustBe 3
    loan.purpose mustBe 2
    loan.occupancy mustBe 1
    loan.amount mustBe 10000
  }

  it should "parse Geography information" in {
    val geography = firstLar.geography

    geography mustBe a[Geography]

    geography.msa mustBe "06920"
    geography.state mustBe "06"
    geography.county mustBe "034"
    geography.tract mustBe "0100.01"
  }

  it should "parse Denial information" in {
    val denial = firstLar.denial

    denial mustBe a[Denial]

    denial.reason1 mustBe "9"
    denial.reason2 mustBe "8"
    denial.reason3 mustBe "7"
  }

  it should "parse Applicant information" in {
    val applicant = firstLar.applicant

    applicant mustBe a[Applicant]

    applicant.ethnicity mustBe 4
    applicant.coEthnicity mustBe 5
    applicant.race1 mustBe 7
    applicant.race2 mustBe "4"
    applicant.race3 mustBe "3"
    applicant.race4 mustBe "2"
    applicant.race5 mustBe "1"
    applicant.coRace1 mustBe 8
    applicant.coRace2 mustBe "7"
    applicant.coRace3 mustBe "6"
    applicant.coRace4 mustBe "5"
    applicant.coRace5 mustBe "4"
    applicant.sex mustBe 1
    applicant.coSex mustBe 2
    applicant.income mustBe "9000"
  }

}
