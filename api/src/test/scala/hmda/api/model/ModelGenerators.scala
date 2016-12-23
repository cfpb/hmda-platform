package hmda.api.model

import java.util.Calendar
import akka.http.scaladsl.model.Uri.Path
import hmda.model.fi._
import hmda.validation.engine._
import org.scalacheck.Gen

trait ModelGenerators {

  implicit def statusGen: Gen[Status] = {
    for {
      status <- Gen.oneOf("OK", "SERVICE_UNAVAILABLE")
      service = "hmda-api"
      time = Calendar.getInstance.getTime.toString
      host = "localhost"
    } yield Status(status, service, time, host)
  }

  implicit def filingStatusGen: Gen[FilingStatus] = {
    Gen.oneOf(NotStarted, InProgress, Completed, Cancelled)
  }

  implicit def filingGen: Gen[Filing] = {
    for {
      id <- Gen.alphaStr
      fid <- Gen.alphaStr
      status <- filingStatusGen
      filingRequired <- Gen.oneOf(true, false)
      start <- Gen.choose(1483287071000L, 1514736671000L)
      end <- Gen.choose(1483287071000L, 1514736671000L)
    } yield Filing(id, fid, status, filingRequired, start, end)
  }

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
      IRSGenerated,
      IRSVerified,
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
    } yield Submission(id, status, start, end)
  }

  implicit def filingDetailGen: Gen[FilingDetail] = {
    for {
      filing <- filingGen
      submissions <- Gen.listOf(submissionGen)
    } yield FilingDetail(filing, submissions)
  }

  implicit def errorResponseGen: Gen[ErrorResponse] = {
    for {
      status <- Gen.oneOf(200, 201, 400, 500)
      message <- Gen.alphaStr
      path <- Gen.alphaStr
    } yield ErrorResponse(status, message, Path(path))
  }

  implicit def larEditResultGen: Gen[LarEditResult] = {
    for {
      loanId <- Gen.alphaStr
    } yield LarEditResult(LarId(loanId))
  }

  implicit def editResultGen: Gen[EditResult] = {
    for {
      edit <- Gen.alphaStr
      description <- Gen.alphaStr
      lars <- Gen.listOf(larEditResultGen)
    } yield EditResult(edit, description, lars)
  }

  implicit def editResultsGen: Gen[EditResults] = {
    for {
      edits <- Gen.listOf(editResultGen)
    } yield EditResults(edits)
  }

  implicit def validationErrorTypeGen: Gen[ValidationErrorType] = {
    Gen.oneOf(
      List(Syntactical, Validity, Quality)
    )
  }

  implicit def syntacticalValidationErrorGen: Gen[SyntacticalValidationError] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
    } yield SyntacticalValidationError(id, name)
  }

  implicit def validityValidationErrorGen: Gen[ValidityValidationError] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
    } yield ValidityValidationError(id, name)
  }

  implicit def qualityValidationErrorGen: Gen[QualityValidationError] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
    } yield QualityValidationError(id, name)
  }

  implicit def macroEditJustificationGen: Gen[MacroEditJustification] = {
    for {
      id <- Gen.choose(Int.MinValue, Int.MaxValue)
      value <- Gen.alphaStr
      verified <- Gen.oneOf(true, false)
      text <- Gen.option(Gen.alphaStr)
    } yield MacroEditJustification(id, value, verified, text)
  }

  implicit def macroEditJustificationWithNameGen: Gen[MacroEditJustificationWithName] = {
    for {
      edit <- Gen.alphaStr
      justification <- macroEditJustificationGen
    } yield MacroEditJustificationWithName(edit, justification)
  }

  implicit def macroValidationErrorGen: Gen[MacroValidationError] = {
    for {
      id <- Gen.alphaStr
      justifications <- Gen.listOf(macroEditJustificationGen)
    } yield MacroValidationError(id, justifications)
  }

  implicit def macroResultGen: Gen[MacroResult] = {
    for {
      id <- Gen.alphaStr
      justification <- Gen.listOf(macroEditJustificationGen)
    } yield MacroResult(id, justification.toSet)
  }

  implicit def summaryEditResultsGen: Gen[SummaryEditResults] = {
    for {
      s <- editResultsGen
      v <- editResultsGen
      q <- editResultsGen
      m <- Gen.listOf(macroResultGen)
    } yield SummaryEditResults(s, v, q, MacroResults(m))
  }

}
