package hmda.api.http.filing.submissions

import akka.NotUsed
import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes, Uri }
import akka.http.scaladsl.server.Directives.{ encodeResponse, handleRejections }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.{ ByteString, Timeout }
import hmda.api.http.directives.QuarterlyFilingAuthorization
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{ cors, corsRejectionHandler }
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.messages.submission.SubmissionProcessingCommands.{ GetHmdaValidationErrorState, GetVerificationStatus }
import hmda.model.filing.submission.{ SubmissionId, SubmissionStatus, VerificationStatus }
import hmda.model.processing.state.{ EditSummary, HmdaValidationErrorState }
import hmda.persistence.submission.{ EditDetailsPersistence, HmdaValidationError }
import hmda.util.http.FilingResponseUtils._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.filing.submissions._
import hmda.auth.OAuth2Authorization
import hmda.messages.submission.EditDetailsCommands.GetEditRowCount
import hmda.messages.submission.EditDetailsEvents.{ EditDetailsAdded, EditDetailsPersistenceEvent, EditDetailsRowCounted }
import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.filing.EditDescriptionLookup
import hmda.query.HmdaQuery._
import hmda.api.http.PathMatchers._
import hmda.persistence.submission.EditDetailsPersistence.selectEditDetailsPersistence
import hmda.persistence.submission.HmdaValidationError.selectHmdaValidationError
import hmda.utils.YearUtils.Period

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.matching.Regex
import scala.util.{ Failure, Success }

trait EditsHttpApi extends HmdaTimeDirectives with QuarterlyFilingAuthorization {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //GET institutions/<lei>/filings/<year>/submissions/<submissionId>/edits
  //GET institutions/<lei>/filings/<year>/quarter/<q>/submissions/<submissionId>/edits
  def editsSummaryPath(oAuth2Authorization: OAuth2Authorization): Route =
    pathPrefix("institutions" / Segment) { lei =>
      timedGet { uri =>
        oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
          path("filings" / Year / "submissions" / IntNumber / "edits") { (year, seqNr) =>
            getEdits(lei, year, None, seqNr, uri)
          } ~ path("filings" / Year / "quarter" / Quarter / "submissions" / IntNumber / "edits") { (year, quarter, seqNr) =>
            pathEndOrSingleSlash {
              quarterlyFilingAllowed(lei, year) {
                getEdits(lei, year, Option(quarter), seqNr, uri)
              }
            }
          }
        }
      }
    }

  private def getEdits(lei: String, year: Int, quarter: Option[String], seqNr: Int, uri: Uri): Route = {
    val submissionId                              = SubmissionId(lei, Period(year, quarter), seqNr)
    val hmdaValidationError                       = selectHmdaValidationError(sharding, submissionId)
    val fEdits: Future[HmdaValidationErrorState]  = hmdaValidationError ? (ref => GetHmdaValidationErrorState(submissionId, ref))
    val fVerification: Future[VerificationStatus] = hmdaValidationError ? (ref => GetVerificationStatus(ref))
    val fEditsAndVer = for {
      edits <- fEdits
      ver   <- fVerification
    } yield (edits, ver)
    onComplete(fEditsAndVer) {
      case Success((edits, ver)) =>
        val syntactical =
          SyntacticalEditSummaryResponse(edits.syntactical.map { editSummary =>
            toEditSummaryResponse(editSummary, submissionId.period)
          }.toSeq.sorted)
        val validity = ValidityEditSummaryResponse(edits.validity.map { editSummary =>
          toEditSummaryResponse(editSummary, submissionId.period)
        }.toSeq.sorted)
        val quality = QualityEditSummaryResponse(edits.quality.map { editSummary =>
          toEditSummaryResponse(editSummary, submissionId.period)
        }.toSeq.sorted, edits.qualityVerified)
        val `macro` = MacroEditSummaryResponse(edits.`macro`.map { editSummary =>
          toEditSummaryResponse(editSummary, submissionId.period)
        }.toSeq.sorted, edits.macroVerified)
        val editsSummaryResponse =
          EditsSummaryResponse(
            syntactical,
            validity,
            quality,
            `macro`,
            SubmissionStatusResponse(
              submissionStatus = SubmissionStatus.valueOf(edits.statusCode),
              verification = ver
            )
          )
        complete(editsSummaryResponse)
      case Failure(e) =>
        failedResponse(StatusCodes.InternalServerError, uri, e)
    }
  }

  //institutions/<lei>/filings/<year>/submissions/<submissionId>/edits/csv
  //institutions/<lei>/filings/<year>/quarter/<q>/submissions/<submissionId>/edits/csv
  def editsSummaryCsvPath(oAuth2Authorization: OAuth2Authorization): Route =
    pathPrefix("institutions" / Segment) { lei =>
      oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
        path("filings" / Year / "submissions" / IntNumber / "edits" / "csv") { (year, seqNr) =>
          csvEditSummaryStream(lei, year, None, seqNr)
        } ~ path("filings" / Year / "quarter" / Quarter / "submissions" / IntNumber / "edits" / "csv") { (year, quarter, seqNr) =>
          pathEndOrSingleSlash {
            quarterlyFilingAllowed(lei, year) {
              csvEditSummaryStream(lei, year, Option(quarter), seqNr)
            }
          }
        }
      }
    }

  private def csvEditSummaryStream(lei: String, year: Int, quarter: Option[String], seqNr: Int): Route = {
    val submissionId = SubmissionId(lei, Period(year, quarter), seqNr)
    val csv = csvHeaderSource
      .concat(validationErrorEventStream(submissionId))
      .map(ByteString(_))
    complete(HttpEntity.Chunked.fromData(ContentTypes.`text/csv(UTF-8)`, csv))
  }

  // GET institutions/<lei>/filings/<year>/submissions/<submissionId>/edits/<edit>
  // GET institutions/<lei>/filings/<year>/quarter/<q>/submissions/<submissionId>/edits/<edit>
  def editDetailsPath(oAuth2Authorization: OAuth2Authorization): Route = {
    val editNameRegex: Regex = new Regex("""[SVQ]\d\d\d(?:-\d)?""")
    pathPrefix("institutions" / Segment) { lei =>
      timedGet { uri =>
        parameters('page.as[Int] ? 1) { page =>
          oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
            path("filings" / Year / "submissions" / IntNumber / "edits" / editNameRegex) { (year, seqNr, editName) =>
              getEditDetails(lei, year, None, seqNr, page, editName, uri)
            } ~ path("filings" / Year / "quarter" / Quarter / "submissions" / IntNumber / "edits" / editNameRegex) {
              (year, quarter, seqNr, editName) =>
                pathEndOrSingleSlash {
                  quarterlyFilingAllowed(lei, year) {
                    getEditDetails(lei, year, Option(quarter), seqNr, page, editName, uri)
                  }
                }
            }
          }
        }
      }
    }
  }

  private def getEditDetails(lei: String, year: Int, quarter: Option[String], seqNr: Int, page: Int, editName: String, uri: Uri): Route = {
    val submissionId                                 = SubmissionId(lei, Period(year, quarter), seqNr)
    val persistenceId                                = s"${EditDetailsPersistence.name}-$submissionId"
    val editDetailsPersistence                       = selectEditDetailsPersistence(sharding, submissionId)
    val fEditRowCount: Future[EditDetailsRowCounted] = editDetailsPersistence ? (ref => GetEditRowCount(editName, ref))
    val fDetails: Future[EditDetailsSummary] = for {
      editRowCount <- fEditRowCount
      s            = EditDetailsSummary(editName, Nil, uri.path.toString(), page, editRowCount.count)
      summary      <- editDetails(persistenceId, s)
    } yield summary

    onComplete(fDetails) {
      case Success(summary) =>
        complete(ToResponseMarshallable(summary))
      case Failure(e) =>
        failedResponse(StatusCodes.InternalServerError, uri, e)
    }
  }

  def editsRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          editsSummaryPath(oAuth2Authorization) ~ editDetailsPath(oAuth2Authorization) ~ editsSummaryCsvPath(oAuth2Authorization)
        }
      }
    }

  private def toEditSummaryResponse(e: EditSummary, period: Period): EditSummaryResponse =
    EditSummaryResponse(e.editName, EditDescriptionLookup.lookupDescription(e.editName, period))

  private def editDetails(persistenceId: String, summary: EditDetailsSummary): Future[EditDetailsSummary] = {
    val editDetails = eventEnvelopeByPersistenceId(persistenceId)
      .map(envelope => envelope.event.asInstanceOf[EditDetailsPersistenceEvent])
      .collect {
        case EditDetailsAdded(editDetail) => editDetail
      }
      .filter(e => e.edit == summary.editName)
      .drop(summary.fromIndex)
      .take(summary.count)
      .runWith(Sink.seq)
    editDetails.map(e => summary.copy(rows = e.flatMap(r => r.rows)))
  }

  private val csvHeaderSource =
    Source.fromIterator(() => Iterator("editType,editId,ULI,editDescription\n"))

  private def validationErrorEventStream(submissionId: SubmissionId): Source[String, NotUsed] = {
    val persistenceId = s"${HmdaValidationError.name}-$submissionId"
    eventsByPersistenceId(persistenceId).collect {
      case evt @ HmdaRowValidatedError(_, _) => evt
    }.mapConcat(
        e =>
          e.validationErrors.map(
            e =>
              EditsCsvResponse(
                e.validationErrorType.toString,
                e.editName,
                e.uli,
                EditDescriptionLookup.lookupDescription(e.editName, submissionId.period)
              )
          )
      )
      .map(_.toCsv)
  }

}
