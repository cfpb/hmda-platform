package hmda.api.http.codec.filing.submission

import hmda.api.http.model.filing.submissions.ParsingErrorSummary
import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowParsedError
import org.scalacheck.Gen
import hmda.model.submission.SubmissionGenerator._

object ParsingErrorSummaryGenerator {

  implicit def parsingErrorSummaryGen: Gen[ParsingErrorSummary] =
    for {
      tsErrors <- Gen.listOf(Gen.alphaStr)
      larErrors <- Gen.listOf(hmdaRowParserErrorGen)
      path <- Gen.alphaStr
      currentPage <- Gen.choose(1, Int.MaxValue)
      total <- Gen.choose(1, Int.MaxValue)
      status <- submissionStatusGen
    } yield
      ParsingErrorSummary(tsErrors, larErrors, path, currentPage, total, status)

  implicit def hmdaRowParserErrorGen: Gen[HmdaRowParsedError] =
    for {
      rowNumber <- Gen.choose(1, 100)
      errorMessages <- Gen.listOf(Gen.alphaStr)
    } yield HmdaRowParsedError(rowNumber, errorMessages)
}
