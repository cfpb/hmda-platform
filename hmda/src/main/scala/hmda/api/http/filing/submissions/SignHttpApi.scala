package hmda.api.http.filing.submissions

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives.{ encodeResponse, handleRejections, _ }
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Sink
import akka.util.{ ByteString, Timeout }
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{ cors, corsRejectionHandler }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.filing.submissions.{ EditsSign, SignedResponse }
import hmda.auth.OAuth2Authorization
import hmda.messages.institution.InstitutionCommands.{ GetInstitution, ModifyInstitution }
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionProcessingCommands.SignSubmission
import hmda.messages.submission.SubmissionProcessingEvents.{ SubmissionNotReadyToBeSigned, SubmissionSigned, SubmissionSignedEvent }
import hmda.model.filing.submission.{ Submission, SubmissionId }
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.{ Institution, Respondent }
import hmda.parser.filing.ts.TsCsvParser
import hmda.persistence.institution.InstitutionPersistence.selectInstitution
import hmda.persistence.submission.HmdaValidationError.selectHmdaValidationError
import hmda.persistence.submission.SubmissionPersistence.selectSubmissionPersistence
import hmda.query.HmdaQuery.readRawData
import hmda.util.http.FilingResponseUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.utils.YearUtils.Period
import org.slf4j.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object SignHttpApi {
  def create(log: Logger, sharding: ClusterSharding)(implicit t: Timeout, ec: ExecutionContext, system: ActorSystem[_]): OAuth2Authorization => Route =
    new SignHttpApi(log, sharding)(t, ec, system).signRoutes _
}

private class SignHttpApi(log: Logger, sharding: ClusterSharding)(implicit t: Timeout, ec: ExecutionContext, system: ActorSystem[_]) {

  // GET & POST institutions/<lei>/filings/<year>/submissions/<submissionId>/sign
  // GET & POST institutions/<lei>/filings/<year>/quarter/<q>/submissions/<submissionId>/sign
  def signPath(oAuth2Authorization: OAuth2Authorization): Route =
    pathPrefix("institutions" / Segment / "filings" / IntNumber) { (lei, year) =>
      pathPrefix("submissions" / IntNumber / "sign") { seqNr =>
        (extractUri & get) { uri =>
          oAuth2Authorization.authorizeTokenWithLei(lei)(token => getSubmissionForSigning(lei, year, None, seqNr, token.email, uri))
        } ~ (extractUri & post) { uri =>
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            oAuth2Authorization.authorizeTokenWithLei(lei) { token =>
              entity(as[EditsSign])(editsSign => signSubmission(lei, year, None, seqNr, token.email, editsSign.signed, uri, token.username))
            }
          }
        }
      } ~ pathPrefix("quarter" / Segment / "submissions" / IntNumber / "sign") { (quarter, seqNr) =>
        (extractUri & get) { uri =>
          oAuth2Authorization.authorizeTokenWithLei(lei) { token =>
            getSubmissionForSigning(lei, year, Option(quarter), seqNr, token.email, uri)
          }
        } ~ (extractUri & post) { uri =>
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            oAuth2Authorization.authorizeTokenWithLei(lei) { token =>
              entity(as[EditsSign])(editsSign => signSubmission(lei, year, Option(quarter), seqNr, token.email, editsSign.signed, uri, token.username))
            }
          }
        }
      }
    }

  private def getSubmissionForSigning(lei: String, year: Int, quarter: Option[String], seqNr: Int, email: String, uri: Uri): Route = {
    val submissionId                            = SubmissionId(lei, Period(year, quarter), seqNr)
    val submissionPersistence                   = selectSubmissionPersistence(sharding, submissionId)
    val fSubmission: Future[Option[Submission]] = submissionPersistence ? GetSubmission
    onComplete(fSubmission) {
      case Failure(e) =>
        failedResponse(StatusCodes.InternalServerError, uri, e)

      case Success(None) =>
        submissionNotAvailable(submissionId, uri)

      case Success(Some(submission)) =>
        val signed = SignedResponse(email, submission.end, submission.receipt, submission.status, submission.signerUsername)
        complete(signed)
    }
  }

  private def signSubmission(
                              lei: String,
                              year: Int,
                              quarter: Option[String],
                              seqNr: Int,
                              email: String,
                              signed: Boolean,
                              uri: Uri,
                              username: String
                            ): Route = {
    log.info(s"inside signSubmission ${lei} and ${seqNr}")
    val submissionId = SubmissionId(lei, Period(year, quarter), seqNr)
    if (!signed) badRequest(submissionId, uri, "Illegal argument: signed = false")
    else {
      val hmdaValidationError                    = selectHmdaValidationError(sharding, submissionId)
      val institutionPersistence = selectInstitution(sharding, lei, year)
      val fSigned: Future[SubmissionSignedEvent] = hmdaValidationError ? (ref => SignSubmission(submissionId, ref, email, username))

      onComplete(fSigned) {
        case Failure(e) =>
          failedResponse(StatusCodes.InternalServerError, uri, e)

        case Success(submissionSignedEvent) =>
          submissionSignedEvent match {
            case signed @ SubmissionSigned(_, _, status) =>
              val signedResponse = SignedResponse(email, signed.timestamp, signed.receipt, status, Some(username))

              for {
                originalInstitution <- institutionPersistence ? GetInstitution
                tsRes <- readRawData(submissionId)
                  .take(1)
                  .map(_.data)
                  .map(ByteString(_))
                  .via(framing("\n"))
                  .map(_.utf8String.trim)
                  .map(s => TsCsvParser(s, fromCassandra = true))
                  .runWith(Sink.head)
                _ = tsRes match {
                  case Right(ts) =>
                    institutionPersistence ? (ref => ModifyInstitution(getUpdatedInstitution(Institution.empty, originalInstitution, ts), ref))
                  case Left(e) => log.error("Failed to modify actor persistence institution.\n{}", e)
                }
              } yield ()

              complete(ToResponseMarshallable(signedResponse))

            case SubmissionNotReadyToBeSigned(id) =>
              badRequest(id, uri, s"Submission $id is not ready to be signed")
          }
      }
    }
  }

  def signRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          signPath(oAuth2Authorization)
        }
      }
    }

  private def getUpdatedInstitution(
    incomingInstitution: Institution,
    originalInstOpt: Option[Institution],
    transmittalSheet: TransmittalSheet,
  ): Institution = {
    val originalFilerFlag = originalInstOpt.getOrElse(Institution.empty).hmdaFiler
    val originalHasFiledQ1Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ1
    val originalHasFiledQ2Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ2
    val originalHasFiledQ3Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ3
    val institutionId_2017Flag =originalInstOpt.getOrElse(Institution.empty).institutionId_2017
    val emailDomainsFlag = originalInstOpt.getOrElse(Institution.empty).emailDomains

    val iFilerFlagsSet = incomingInstitution.copy(
      LEI = transmittalSheet.LEI,
      activityYear = transmittalSheet.year,
      agency = transmittalSheet.agency,
      taxId = Some(transmittalSheet.taxId),
      respondent = Respondent(
        Some(transmittalSheet.institutionName),
        Some(transmittalSheet.contact.address.state),
        Some(transmittalSheet.contact.address.city)),
      institutionId_2017 = institutionId_2017Flag,
      emailDomains = emailDomainsFlag,
      hmdaFiler = originalFilerFlag,
      quarterlyFilerHasFiledQ1 = originalHasFiledQ1Flag,
      quarterlyFilerHasFiledQ2 = originalHasFiledQ2Flag,
      quarterlyFilerHasFiledQ3 = originalHasFiledQ3Flag
    )

    iFilerFlagsSet
  }
}