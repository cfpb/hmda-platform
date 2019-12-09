package hmda.api.http.codec.filing.submission

import hmda.api.http.model.filing.submissions.{ParsingErrorSummary, HmdaRowParsedErrorSummary, FieldParserErrorSummary}
import org.scalacheck.Gen
import hmda.model.submission.SubmissionGenerator._

object ParsingErrorSummaryGenerator {

  implicit def FieldParserErrorSummaryGen: Gen[FieldParserErrorSummary] =
    for {
      fieldName <- Gen.alphaStr
      inputValue <- Gen.alphaStr
      validValues <- Gen.alphaStr
    } yield FieldParserErrorSummary(fieldName, inputValue, validValues)

  implicit def parsingErrorSummaryGen: Gen[ParsingErrorSummary] =
    for {
      tsErrors <- Gen.listOf(hmdaRowParserErrorGen)
      larErrors <- Gen.listOf(hmdaRowParserErrorGen)
      path <- Gen.alphaStr.suchThat(_.length > 0)
      currentPage <- Gen.choose(1, Int.MaxValue)
      total <- Gen.choose(1, Int.MaxValue)
      status <- submissionStatusGen
    } yield
      ParsingErrorSummary(tsErrors, larErrors, path, currentPage, total, status)

  implicit def hmdaRowParserErrorGen: Gen[HmdaRowParsedErrorSummary] =
    for {
      rowNumber <- Gen.choose(1, 100)
      estimatedULI <- Gen.alphaStr
      errorMessages <- Gen.listOf(FieldParserErrorSummaryGen)
    } yield HmdaRowParsedErrorSummary(rowNumber, estimatedULI, errorMessages)
}
