package hmda.api.model

import java.util.Calendar

import hmda.model.fi._
import org.scalacheck.{ Arbitrary, Gen }

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
      status <- institutionStatusGen
    } yield Institution(id, name, status)
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
      ValidatingSyntaxAndValidity,
      ValidatedSyntaxAndValidity,
      ValidatingQualityAndMacro,
      Unverified,
      Verified,
      Signed
    )
  }

  implicit def submissionGen: Gen[Submission] = {
    for {
      id <- Gen.choose(0, Int.MaxValue)
      status <- submissionStatusGen
    } yield Submission(id, status)
  }

}
