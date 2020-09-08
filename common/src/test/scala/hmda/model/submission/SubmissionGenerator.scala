package hmda.model.submission

import hmda.model.filing.submission._
import org.scalacheck.Gen
import hmda.utils.YearUtils.Period

object SubmissionGenerator {

  implicit def submissionStatusGen: Gen[SubmissionStatus] = {
    Gen.oneOf(
      Created,
      Uploading,
      Uploaded,
      Parsing,
      ParsedWithErrors,
      Parsed,
      Validating,
      SyntacticalOrValidity,
      SyntacticalOrValidityErrors,
      Quality,
      QualityErrors,
      Macro,
      MacroErrors,
      Verified,
      Signed
    )
  }

  implicit def submissionIdGen: Gen[SubmissionId] = {
    for {
      institutionId <- Gen.alphaStr
      year <- Gen.choose(2018,2020)
      seqNr <- Gen.choose(0, Int.MaxValue)
    } yield SubmissionId(institutionId, Period(year, None), seqNr)
  }

  implicit def submissionGen: Gen[Submission] = {
    for {
      id <- submissionIdGen
      status <- submissionStatusGen
      start <- Gen.choose(1483287071000L, 1514736671000L)
      end <- Gen.choose(1483287071000L, 1514736671000L)
      fileName <- Gen.alphaStr
      receipt <- Gen.alphaStr
      username <- Gen.option(Gen.asciiStr.filter(_.nonEmpty))
    } yield Submission(id, status, start, end, fileName, receipt, username)
  }

}
