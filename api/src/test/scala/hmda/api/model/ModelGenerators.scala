package hmda.api.model

import java.util.Calendar
import akka.http.scaladsl.model.Uri.Path
import hmda.model.fi._
import hmda.model.institution._
import hmda.validation.engine._
import org.scalacheck.Gen

trait ModelGenerators {

  implicit def statusGen: Gen[Status] = {
    for {
      status <- Gen.oneOf("OK", "SERVICE_UNAVAILABLE")
      service = "hmda-api"
      time = Calendar.getInstance().getTime().toString
      host = "localhost"
    } yield Status(status, service, time, host)
  }

  implicit def institutionStatusGen: Gen[InstitutionStatus] = {
    Gen.oneOf(Active, Inactive)
  }

  implicit def institutionGen: Gen[Institution] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      externalIds <- Gen.listOf(externalIdGen)
      status <- institutionStatusGen
      agency <- agencyGen
      active <- Gen.oneOf(true, false)
      cra <- Gen.oneOf(true, false)
      institutionType <- institutionTypeGen
    } yield Institution(id, name, externalIds.toSet, agency, institutionType, active, cra, status)
  }

  implicit def filingStatusGen: Gen[FilingStatus] = {
    Gen.oneOf(NotStarted, InProgress, Completed, Cancelled)
  }

  implicit def filingGen: Gen[Filing] = {
    for {
      id <- Gen.alphaStr
      fid <- Gen.alphaStr
      status <- filingStatusGen
    } yield Filing(id, fid, status)
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
    } yield Submission(id, status)
  }

  implicit def filingDetailGen: Gen[FilingDetail] = {
    for {
      filing <- filingGen
      submissions <- Gen.listOf(submissionGen)
    } yield FilingDetail(filing, submissions)
  }

  implicit def agencyGen: Gen[Agency] = {
    Gen.oneOf(
      Agency.values
    )
  }

  implicit def institutionTypeGen: Gen[InstitutionType] = {
    Gen.oneOf(
      InstitutionType.values
    )
  }

  implicit def externalIdGen: Gen[ExternalId] = {
    for {
      id <- Gen.alphaStr
      idType <- externalIdTypeGen
    } yield ExternalId(id, idType)
  }

  implicit def externalIdTypeGen: Gen[ExternalIdType] = {
    Gen.oneOf(
      ExternalIdType.values
    )
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
      ts <- Gen.oneOf(true, false)
      lars <- Gen.listOf(larEditResultGen)
    } yield EditResult(edit, ts, lars)
  }

  implicit def editResultsGen: Gen[EditResults] = {
    for {
      edits <- Gen.listOf(editResultGen)
    } yield EditResults(edits)
  }

  implicit def validationErrorTypeGen: Gen[ValidationErrorType] = {
    Gen.oneOf(
      List(Syntactical, Validity, Quality, Macro)
    )
  }

  implicit def validationErrorGen: Gen[ValidationError] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      errorType <- validationErrorTypeGen
    } yield ValidationError(id, name, errorType)
  }

  implicit def summaryEditResultsGen: Gen[SummaryEditResults] = {
    for {
      s <- editResultsGen
      v <- editResultsGen
      q <- editResultsGen
      m <- editResultsGen
    } yield SummaryEditResults(s, v, q, m)
  }

}
