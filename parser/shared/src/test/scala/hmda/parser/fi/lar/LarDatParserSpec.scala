package hmda.parser.fi.lar

import hmda.model.fi.lar._
import hmda.model.util.FITestData
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class LarDatParserSpec extends PropSpec with MustMatchers with PropertyChecks with LarGenerators {

  import FITestData._

  val lars = larsDAT.map(line => LarDatParser(line))
  val firstLar = lars.head

  property("LAR Parser should parse correct number of LARs") {
    lars.size mustBe 3
  }

  //LarDatParser fails with Scala.js
  //  property("LAR Parser should parse all generated LARs") {
  //    forAll(larGen) { (lar: LoanApplicationRegister) =>
  //      val parsedLar = LarDatParser(lar.toDAT)
  //      val updatedApplicant = parsedLar.applicant.copy(income = parsedLar.applicant.income.replaceFirst("^0+(?!$)", ""))
  //      val updatedLar = parsedLar.copy(
  //        respondentId = parsedLar.respondentId.replaceFirst("^0+(?!$)", ""),
  //        applicant = updatedApplicant
  //      )
  //      updatedLar mustBe lar
  //    }
  //  }

  property("LAR Parser should parse basic LAR information") {
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

  property("LAR Parser should parse loan information") {
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

  property("LAR Parser should parse Geography information") {
    val geography = firstLar.geography

    geography mustBe a[Geography]

    geography.msa mustBe "06920"
    geography.state mustBe "06"
    geography.county mustBe "034"
    geography.tract mustBe "0100.01"
  }

  property("LAR Parser should parse Denial information") {
    val denial = firstLar.denial

    denial mustBe a[Denial]

    denial.reason1 mustBe "9"
    denial.reason2 mustBe "8"
    denial.reason3 mustBe "7"
  }

  property("LAR Parser should parse Applicant information") {
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

  private def padLeftWithZero(s: String, n: Int): String = {
    String.format("%1$" + n + "s", s).replace(' ', '0')
  }

}
