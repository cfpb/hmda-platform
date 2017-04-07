package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.institutions.submissions._
import hmda.api.http.institutions.{ FilingPaths, InstitutionPaths, UploadPaths }
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }

import scala.concurrent.ExecutionContext

trait InstitutionsHttpApi
    extends InstitutionProtocol
    with InstitutionPaths
    with FilingPaths
    with SubmissionBasePaths
    with SubmissionParseErrorsPaths
    with SubmissionEditPaths
    with SubmissionIrsPaths
    with SubmissionSignPaths
    with SubmissionSummaryPaths
    with UploadPaths
    with ApiErrorProtocol
    with HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val institutionsRoutes =
    extractExecutionContext { executor =>
      implicit val ec: ExecutionContext = executor
      encodeResponse {
        headerAuthorize {
          institutionsPath ~
            pathPrefix("institutions" / Segment) { instId =>
              institutionAuthorize(instId) {
                institutionByIdPath(instId) ~
                  filingByPeriodPath(instId) ~
                  submissionPath(instId) ~
                  submissionLatestPath(instId) ~
                  uploadPath(instId) ~
                  submissionEditsPath(instId) ~
                  submissionParseErrorsPath(instId) ~
                  submissionEditCsvPath(instId) ~
                  submissionSingleEditPath(instId) ~
                  editFailureDetailsPath(instId) ~
                  verifyEditsPath(instId) ~
                  submissionIrsPath(instId) ~
                  submissionSignPath(instId) ~
                  submissionSummaryPath(instId)
              }
            }
        }
      }
    }
}
