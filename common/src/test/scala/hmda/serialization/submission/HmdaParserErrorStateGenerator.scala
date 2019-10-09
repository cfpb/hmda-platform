package hmda.serialization.submission

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  HmdaRowParsedError
}
import hmda.model.processing.state.HmdaParserErrorState
import org.scalacheck.Gen
import hmda.messages.submission.SubmissionProcessingCommands.FieldParserError

object HmdaParserErrorStateGenerator {

  implicit def FieldParserErrorGen: Gen[FieldParserError] =
    for {
      fieldName <- Gen.alphaStr
      inputValue <- Gen.alphaStr
    } yield FieldParserError(fieldName, inputValue)

  implicit def hmdaRowParsedErrorGen: Gen[HmdaRowParsedError] =
    for {
      rowNumber <- Gen.choose(0, Int.MaxValue)
      estimatedULI <- Gen.alphaStr
      errors <- Gen.listOf(FieldParserErrorGen)
    } yield HmdaRowParsedError(rowNumber, estimatedULI, errors)

  implicit def hmdaRowParsedCountGen: Gen[HmdaRowParsedCount] =
    for {
      i <- Gen.choose(0, Int.MaxValue)
    } yield HmdaRowParsedCount(i)

  implicit def hmdaParserErrorStateGen: Gen[HmdaParserErrorState] =
    for {
      tsErrors <- Gen.listOfN(1, hmdaRowParsedErrorGen)
      larErrors <- Gen.listOf(hmdaRowParsedErrorGen)
    } yield
      HmdaParserErrorState(tsErrors, larErrors, tsErrors.size + larErrors.size)
}
