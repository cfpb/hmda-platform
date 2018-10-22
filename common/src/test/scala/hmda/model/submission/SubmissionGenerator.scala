package hmda.model.submission

import hmda.model.filing.submission._
import org.scalacheck.Gen

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
      fileName <- Gen.alphaStr
      receipt <- Gen.alphaStr
    } yield Submission(id, status, start, end, fileName, receipt)
  }

}
