package hmda.api.http.institutions.submissions

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.query.DbConfiguration
import hmda.query.repository.filing.FilingComponent

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait SubmissionIrsPaths
    extends HmdaCustomDirectives
    with FilingComponent
    with DbConfiguration {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs
  def submissionIrsPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "irs") { (period, submissionId) =>
      timedGet { uri =>
        val larTotalRepository = new LarTotalRepository(config)
        val data = larTotalRepository.getMsaSource()

        onComplete(data) {
          case Success(msaSeq) =>
            val test = msaSeq.toList.toString
            val response = HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, test))

            complete(response)
          case Failure(e) => completeWithInternalError(uri, e)
        }
      }
    }
}
