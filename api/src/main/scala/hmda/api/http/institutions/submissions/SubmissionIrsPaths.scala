package hmda.api.http.institutions.submissions

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.MsaProtocol
import hmda.query.DbConfiguration
import hmda.query.model.filing.{ Irs, Msa, MsaSummary }
import hmda.query.repository.filing.FilingComponent

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait SubmissionIrsPaths
    extends HmdaCustomDirectives
    with MsaProtocol
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
            val msaList = msaSeq.toList
            val irs = Irs(msaList, createSummary(msaList))
            complete(ToResponseMarshallable(irs))
          case Failure(e) => completeWithInternalError(uri, e)
        }
      }
    }

  private def createSummary(msaList: Seq[Msa]): MsaSummary = {
    msaList.foldLeft(MsaSummary.empty) { (summary, msa) => summary + msa }
  }
}
