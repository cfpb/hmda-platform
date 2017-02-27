package hmda.api.http.institutions.submissions

import akka.http.scaladsl.model.HttpEntity.ChunkStreamPart
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
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

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/irs
  def submissionIrsPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "irs") { (period, submissionId) =>
      timedGet { _ =>
        val larTotalRepository = new LarTotalRepository(config)

        val data = larTotalRepository.getMsaSource().map(x => ChunkStreamPart(x.toString))

        val response = HttpResponse(StatusCodes.OK, entity = HttpEntity.Chunked(ContentTypes.`application/json`, data))

        complete(response)
      }
    }
}
