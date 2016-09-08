package hmda.api.model

import java.util.Calendar

import hmda.model.fi._
import hmda.model.institution.Agency._
import hmda.model.institution.ExternalIdType._
import hmda.model.institution._
import hmda.model.institution.InstitutionStatus.{ Active, Inactive }
import hmda.model.institution.InstitutionType._
import org.scalacheck.{ Arbitrary, Gen }

import scalaz.Alpha.B

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
      institutionType <- institutionTypeGen
    } yield Institution(id, name, externalIds.toSet, agency, institutionType, active, status)
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

  implicit def submissionGen: Gen[Submission] = {
    for {
      id <- Gen.choose(0, Int.MaxValue)
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
}
