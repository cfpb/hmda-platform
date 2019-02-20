package hmda.model.filing

import hmda.model.filing.FilingGenerator._
import hmda.model.submission.SubmissionGenerator._
import org.scalacheck.Gen

object FilingDetailsGenerator {

  def filingDetailsGen: Gen[FilingDetails] = {
    for {
      filing <- filingGen
      submissions <- Gen.listOf(submissionGen)
    } yield FilingDetails(filing, submissions)
  }
}
