package hmda.api.http.institutions.submissions

import akka.pattern.ask
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import hmda.query.DbConfiguration
import hmda.query.repository.filing.FilingComponent

import scala.concurrent.ExecutionContext

trait SubmissionIrsPaths
    extends InstitutionProtocol
    with SubmissionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with ValidationErrorConverter
    with FilingComponent
    with DbConfiguration {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs
  // NOTE:  This is currently a mocked, static endpoint
  def submissionIrsPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "irs") { (period, submissionId) =>
      timedGet { uri =>
        val larTotalRepository = new LarTotalRepository(config)

        val data = larTotalRepository.getMsaSource().map(x => ChunkStreamPart(x.toString))

        val response = HttpResponse(StatusCodes.OK, entity = HttpEntity.Chunked(ContentTypes.`application/json`, data))

        complete(response)
      }
    }
}
