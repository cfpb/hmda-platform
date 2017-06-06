package hmda.api.http.institutions.submissions

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.{ Irs, IrsResponse }
import hmda.api.protocol.processing.MsaProtocol
import hmda.census.model.Msa
import hmda.query.repository.filing.FilingComponent
import hmda.validation.ValidationStats.FindIrsStats

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait SubmissionIrsPaths
    extends HmdaCustomDirectives
    with RequestVerificationUtils
    with MsaProtocol
    with FilingComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs
  def submissionIrsPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "irs") { (period, submissionId) =>
      timedGet { uri =>
        completeVerified(institutionId, period, submissionId, uri) {
          parameters('page.as[Int] ? 1) { (page: Int) =>
            val validationStats = system.actorSelection("/user/validation-stats")
            val data = (validationStats ? FindIrsStats(institutionId, period)).mapTo[Seq[Msa]]

            onComplete(data) {
              case Success(msaSeq) =>
                val irs: IrsResponse = Irs(msaSeq).paginatedResponse(page, uri.path.toString)
                complete(ToResponseMarshallable(irs))
              case Failure(e) => completeWithInternalError(uri, e)
            }
          }
        }
      }
    }
}
