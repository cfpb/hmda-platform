package hmda.api.http.institutions.submissions

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.institutions.submissions.{ ContactSummary, FileSummary, RespondentSummary, SubmissionSummary }
import hmda.api.model.{ ErrorResponse, FilingDetail }
import hmda.model.fi.{ Submission, ValidatedWithErrors }
import org.scalatest.BeforeAndAfterAll

class SubmissionSummaryPathsSpec extends InstitutionHttpApiSpec with BeforeAndAfterAll {

  val institutionId = "0"
  val period = "2017"
  val seqNr = 1

  val csv = "1|bflExternalTest0|3|201502221111|2017|35-0704860|10|Passes Bank|555 Passes Court|Passes City|CA|92130|Passes Bank Parent|555 Passes Court Parent|Passes City|CA|92130|Passes Person|555-555-5555|555-555-5555|pperson@passes.com\n" +
    "2|bflExternalTest0|3|10164 |20170224|1|1|3|1|21|3|1|20170326|45460|18|153|0501.00|2|2|5| | | | |5| | | | |1|2|31|0| | | |NA   |2|1\n" +
    "2|bflExternalTest0|3|10174 |20170224|1|1|2|1|60|3|1|20170402|45460|18|153|0503.00|2|2|5| | | | |5| | | | |1|2|210|0| | | |NA   |2|2\n" +
    "2|bflExternalTest0|3|10370 |20170228|1|1|3|1|73|3|3|20170326|45460|18|153|0505.00|2|2|5| | | | |5| | | | |1|2|89|0|4| | |NA   |2|1"

  val fileName = "2017_lars_bank_1.txt"
  val file = multiPartFile(csv, fileName)

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  "Submission Summary Paths" must {

    "Set up: upload a file" in {
      postWithCfpbHeaders(s"/institutions/$institutionId/filings/$period/submissions/$seqNr", file) ~> institutionsRoutes(supervisor, querySupervisor, validationStats) ~> check {
        Thread.sleep(5000) // wait for the submission to complete validation
        status mustBe StatusCodes.Accepted
      }
    }

    "Set up: must have uploaded" in {
      getWithCfpbHeaders(s"/institutions/$institutionId/filings/$period") ~> institutionsRoutes(supervisor, querySupervisor, validationStats) ~> check {
        val subs = responseAs[FilingDetail].submissions
        val sub: Submission = subs.find(_.id.sequenceNumber == seqNr).get
        sub.fileName mustBe fileName
        sub.status mustBe ValidatedWithErrors
      }
    }

    "return a 200" in {
      getWithCfpbHeaders(s"/institutions/$institutionId/filings/$period/submissions/$seqNr/summary") ~> institutionsRoutes(supervisor, querySupervisor, validationStats) ~> check {
        val contactSummary = ContactSummary("Passes Person", "555-555-5555", "pperson@passes.com")
        val respondentSummary = RespondentSummary("Passes Bank", "externalTest0", "35-0704860", "fdic", contactSummary)
        val fileSummary = FileSummary(name = fileName, year = "2017", totalLARS = 3)
        val submissionSummary = SubmissionSummary(respondentSummary, fileSummary)

        println(response)
        status mustBe StatusCodes.OK
        responseAs[SubmissionSummary] mustBe submissionSummary
      }
    }

    "return 404 for nonexistent institution" in {
      getWithCfpbHeaders(s"/institutions/xxxxx/filings/$period/submissions/$seqNr/summary") ~> institutionsRoutes(supervisor, querySupervisor, validationStats) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "Institution xxxxx not found"
      }
    }
    "return 404 for nonexistent filing period" in {
      getWithCfpbHeaders(s"/institutions/$institutionId/filings/1980/submissions/$seqNr/summary") ~> institutionsRoutes(supervisor, querySupervisor, validationStats) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "1980 filing period not found for institution 0"
      }
    }
    "return 404 for nonexistent submission" in {
      getWithCfpbHeaders(s"/institutions/$institutionId/filings/$period/submissions/0/summary") ~> institutionsRoutes(supervisor, querySupervisor, validationStats) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse].message mustBe "Submission 0 not found for 2017 filing period"
      }
    }
  }
}
