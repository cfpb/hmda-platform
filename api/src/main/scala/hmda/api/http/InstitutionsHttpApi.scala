package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.institutions.submissions.{ SubmissionBasePaths, SubmissionEditPaths, SubmissionIrsPaths }
import hmda.api.http.institutions.{ FilingPaths, InstitutionPaths, UploadPaths }
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }

trait InstitutionsHttpApi
    extends InstitutionProtocol
    with InstitutionPaths
    with FilingPaths
    with SubmissionBasePaths
    with SubmissionEditPaths
    with SubmissionIrsPaths
    with UploadPaths
    with ApiErrorProtocol
    with HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val institutionsRoutes =
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
              submissionSingleEditPath(instId) ~
              irsPath(instId)
          }
        }
    }
}
