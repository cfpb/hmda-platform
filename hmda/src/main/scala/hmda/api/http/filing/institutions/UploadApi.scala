package hmda.api.http.filing.institutions

import java.time.Instant

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import hmda.api.http.directives.HmdaTimeDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.model.filing.submission.SubmissionId

trait UploadApi extends HmdaTimeDirectives {

  // institutions/<institutionId>/filings/<period>/submissions/<seqNr>
  def uploadHmdaFileRoute: Route =
    path(Segment / "filings" / Segment / "submissions" / IntNumber) {
      (lei, period, seqNr) =>
        timedPost { uri =>
          val submissionId = SubmissionId(lei, period, seqNr)
          val uploadTimestamp = Instant.now.toEpochMilli
          complete("Uploaded")
        }
    }

  def uploadRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("institutions") {
            uploadHmdaFileRoute
          }
        }
      }
    }
  }

}
