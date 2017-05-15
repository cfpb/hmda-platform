package hmda.model.institution

import hmda.model.fi._
import org.scalacheck.Gen

object SubmissionGenerators {

  implicit def submissionStatusGen: Gen[SubmissionStatus] = {
    Gen.oneOf(
      Created,
      Uploading,
      Uploaded,
      Parsing,
      Parsed,
      ParsedWithErrors,
      Validating,
      ValidatedWithErrors,
      Validated,
      Signed
    )
  }

  implicit def submissionIdGen: Gen[SubmissionId] = {
    for {
      institutionId <- Gen.alphaStr
      period <- Gen.alphaStr
      seqNr <- Gen.choose(0, Int.MaxValue)
    } yield SubmissionId(institutionId, period, seqNr)
  }

  implicit def submissionGen: Gen[Submission] = {
    for {
      id <- submissionIdGen
      status <- submissionStatusGen
      start <- Gen.choose(1483287071000L, 1514736671000L)
      end <- Gen.choose(1483287071000L, 1514736671000L)
      receipt <- Gen.alphaStr
    } yield Submission(id, status, start, end, receipt)
  }
}
