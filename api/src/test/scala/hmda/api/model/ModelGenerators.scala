package hmda.api.model

import java.util.Calendar
import akka.http.scaladsl.model.Uri.Path
import hmda.model.fi._
import hmda.validation.engine._
import org.scalacheck.Gen
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.model.fi.lar.fields.LarApplicantFields._
import hmda.model.fi.lar.fields.LarDenialFields._
import hmda.model.fi.lar.fields.LarGeographyFields._
import hmda.model.fi.lar.fields.LarLoanFields._

trait ModelGenerators {

  implicit def statusGen: Gen[Status] = {
    for {
      status <- Gen.oneOf("OK", "SERVICE_UNAVAILABLE")
      service = "hmda-api"
      time = Calendar.getInstance().getTime.toString
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
      start <- Gen.choose(1483287071000L, 1514736671000L)
      end <- Gen.choose(1483287071000L, 1514736671000L)
    } yield Filing(id, fid, status, start, end)
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

  implicit def fieldGen: Gen[RecordField] = {
    Gen.oneOf(
      noField,
      respondentId,
      agencyCode,
      preaprovals,
      actionTakenType,
      actionTakenDate,
      purchaserType,
      rateSpread,
      heopaStatus,
      lienStatus,
      ethnicity,
      coEthnicity,
      race1,
      race2,
      race3,
      race4,
      race5,
      coRace1,
      coRace2,
      coRace3,
      coRace4,
      coRace5,
      sex,
      coSex,
      income,
      reason1,
      reason2,
      reason3,
      msa,
      state,
      county,
      tract,
      id,
      applicationDate,
      loanType,
      propertyType,
      purpose,
      occupancy,
      amount
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
      fields <- Gen.alphaStr
      ts <- Gen.oneOf(true, false)
      lars <- Gen.listOf(larEditResultGen)
    } yield EditResult(edit, description, List(fields), ts, lars)
  }

  implicit def editResultsGen: Gen[EditResults] = {
    for {
      edits <- Gen.listOf(editResultGen)
    } yield EditResults(edits)
  }

  implicit def justificationGen: Gen[Justification] = {
    for {
      value <- Gen.alphaStr
      selected <- Gen.oneOf(true, false)
    } yield Justification(value, selected)
  }

  implicit def macroResultGen: Gen[MacroResult] = {
    for {
      id <- Gen.alphaStr
      justification <- Gen.listOf(justificationGen)
    } yield MacroResult(id, justification)
  }

  implicit def validationErrorTypeGen: Gen[ValidationErrorType] = {
    Gen.oneOf(
      List(Syntactical, Validity, Quality)
    )
  }

  implicit def validationErrorGen: Gen[ValidationError] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      field <- fieldGen
      fieldDescription <- Gen.alphaStr
      errorType <- validationErrorTypeGen
    } yield ValidationError(id, ValidationErrorMetaData(name), errorType)
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
