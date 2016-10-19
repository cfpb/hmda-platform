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

          val partialPath = "src/main/scala/hmda/api/http/institutions/submissions/tempJson/summary.json"
          val source = Try(Source.fromFile(new File(partialPath))).getOrElse(Source.fromFile(new File("api/" + partialPath)))

          val summaryJson = source.getLines.mkString
          val response = HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`application/json`, summaryJson))

          complete(response)
        }
      }
    }
}
