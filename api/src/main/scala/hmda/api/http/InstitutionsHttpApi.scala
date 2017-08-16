package hmda.api.http

import akka.actor.ActorRef
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

  def institutionsRoutes(supervisor: ActorRef, querySupervisor: ActorRef, validationStats: ActorRef) =
    extractExecutionContext { executor =>
      implicit val ec: ExecutionContext = executor
      encodeResponse {
        headerAuthorize {
          institutionsPath(querySupervisor) ~
            pathPrefix("institutions" / Segment) { instId =>
              institutionAuthorize(instId) {
                institutionByIdPath(supervisor, querySupervisor, instId) ~
                  filingByPeriodPath(supervisor, instId) ~
                  submissionPath(instId) ~
                  submissionLatestPath(instId) ~
                  uploadPath(supervisor, querySupervisor, instId) ~
                  submissionEditsPath(instId) ~
                  submissionParseErrorsPath(instId) ~
                  submissionEditCsvPath(instId) ~
                  submissionSingleEditPath(instId) ~
                  editFailureDetailsPath(instId) ~
                  verifyEditsPath(instId) ~
                  submissionIrsPath(validationStats, instId) ~
                  submissionIrsCsvPath(validationStats, instId) ~
                  submissionSignPath(supervisor, instId) ~
                  submissionSummaryPath(supervisor, instId)
              }
            }
        }
      }
    }
}
