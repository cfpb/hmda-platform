package hmda.api.http.institutions.submissions

import java.io.File

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.Try

trait SubmissionSummaryPaths
    extends InstitutionProtocol
    with SubmissionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with ValidationErrorConverter {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/summary
  // NOTE:  This is currently a mocked, static endpoint
  def submissionSummaryPath(institutionId: String) =
    path("filings" / Segment / "submissions" / IntNumber / "summary") { (period, submissionId) =>
      extractExecutionContext { executor =>
        timedGet { uri =>
          implicit val ec: ExecutionContext = executor
          val supervisor = system.actorSelection("/user/supervisor")

          //To avoid having to deal with relative paths on different systems
          val summaryJson = "{\n  \"respondent\": {\n    \"name\": \"Bank\",\n    \"id\": \"1234567890\",\n    \"taxId\": \"0987654321\",\n    \"agency\": \"CFPB\",\n    \"contact\": {\n      \"name\": \"Your Name\",\n      \"phone\": \"123-456-7890\",\n      \"email\": \"your.name@bank.com\"\n    }\n  },\n  \"file\": {\n    \"name\": \"lar.dat\",\n    \"year\": \"2016\",\n    \"totalLARS\": 25\n  }\n}"
          val response = HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, summaryJson))

          complete(response)
        }
      }
    }
}
