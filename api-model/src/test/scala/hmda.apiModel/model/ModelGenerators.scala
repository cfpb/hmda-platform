package hmda.apiModel.model

import java.util.Calendar

import akka.http.scaladsl.model.Uri.Path
import hmda.apiModel.model.institutions.submissions.{ ContactSummary, FileSummary, RespondentSummary, SubmissionSummary }
import hmda.apiModel.model.public.InstitutionSearch
import org.scalacheck.Gen
import spray.json.{ JsObject, JsString }
import hmda.model.institution.InstitutionGenerators._
import hmda.model.institution.FilingGenerators._
import hmda.model.institution.SubmissionGenerators._

trait ModelGenerators {

  implicit def statusGen: Gen[Status] = {
    for {
      status <- Gen.oneOf("OK", "SERVICE_UNAVAILABLE")
      service = "hmda-api"
      time = Calendar.getInstance.getTime.toString
      host = "localhost"
    } yield Status(status, service, time, host)
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

  implicit def fieldsGen: Gen[JsObject] = {
    for {
      str1 <- Gen.alphaStr
      str2 <- Gen.alphaStr
    } yield JsObject((str1, JsString(str2)), (str2, JsString(str1)))
  }

  implicit def larEditResultGen: Gen[EditResultRow] = {
    for {
      loanId <- Gen.alphaStr
      fields <- fieldsGen
    } yield EditResultRow(RowId(loanId), fields)
  }

  implicit def editInfoGen: Gen[EditInfo] = {
    for {
      name <- Gen.alphaStr
      desc <- Gen.alphaStr
    } yield EditInfo(name, desc)
  }

  implicit def editCollectionGen: Gen[EditCollection] = {
    for {
      e <- Gen.listOf(editInfoGen)
    } yield EditCollection(e)
  }

  implicit def verifiableEditCollectionGen: Gen[VerifiableEditCollection] = {
    for {
      b <- Gen.oneOf(true, false)
      e <- Gen.listOf(editInfoGen)
    } yield VerifiableEditCollection(b, e)
  }

  implicit def summaryEditResultsGen: Gen[SummaryEditResults] = {
    for {
      s <- editCollectionGen
      v <- editCollectionGen
      q <- verifiableEditCollectionGen
      m <- verifiableEditCollectionGen
      st <- submissionStatusGen
    } yield SummaryEditResults(s, v, q, m, st)
  }

  implicit def institutionSearchGen: Gen[InstitutionSearch] = {
    for {
      id <- Gen.alphaStr
      name <- Gen.alphaStr
      domains <- Gen.listOfN(3, Gen.alphaStr)
      externalIds <- Gen.listOf(externalIdGen)
    } yield InstitutionSearch(id, name, domains.toSet, externalIds.toSet)
  }

  implicit def institutionSearchGenList: Gen[List[InstitutionSearch]] = {
    Gen.listOf(institutionSearchGen)
  }

  implicit def fileSummaryGen: Gen[FileSummary] = {
    for {
      name <- Gen.alphaStr
      year <- Gen.alphaStr
      totalLars <- Gen.choose(Int.MinValue, Int.MaxValue)
    } yield FileSummary(name, year, totalLars)
  }

  implicit def contactSummaryGen: Gen[ContactSummary] = {
    for {
      name <- Gen.alphaStr
      phone <- Gen.alphaStr
      email <- Gen.alphaStr
    } yield ContactSummary(name, phone, email)
  }

  implicit def respondentSummaryGen: Gen[RespondentSummary] = {
    for {
      name <- Gen.alphaStr
      id <- Gen.alphaStr
      taxId <- Gen.alphaStr
      agency <- Gen.alphaStr
      contact <- contactSummaryGen
    } yield RespondentSummary(name, id, taxId, agency, contact)
  }

  implicit def submissionSummaryGen: Gen[SubmissionSummary] = {
    for {
      respondent <- respondentSummaryGen
      file <- fileSummaryGen
    } yield SubmissionSummary(respondent, file)
  }

}
