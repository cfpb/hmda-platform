package hmda.api.http.institutions.submissions

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model.institutions.submissions.{ FileSummary, RespondentSummary, SubmissionSummary, ContactSummary }
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import scala.concurrent.ExecutionContext

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
  def submissionSummaryPath(institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "summary") { (period, submissionId) =>
      timedGet { uri =>
        val supervisor = system.actorSelection("/user/supervisor")

        val contactSummary = ContactSummary("Your Name", "123-456-7890", "your.name@bank.com")
        val respondentSummary = RespondentSummary("Bank", "1234567890", "0987654321", "CFPB", contactSummary)
        val fileSummary = FileSummary("lar.dat", "2016", 25)

        val submissionSummary = SubmissionSummary(respondentSummary, fileSummary)

        complete(ToResponseMarshallable(submissionSummary))
      }
    }
}
