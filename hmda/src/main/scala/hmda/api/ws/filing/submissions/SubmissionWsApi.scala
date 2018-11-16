package hmda.api.ws.filing.submissions

import akka.NotUsed
import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.model.ws.Message
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Source}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.filing.submission.SubmissionId

import scala.concurrent.duration._

trait SubmissionWsApi {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  def producer(submissionId: SubmissionId): Source[String, NotUsed] =
    Source(1 to 1000)
      .throttle(1, 1.second, 1, ThrottleMode.Shaping)
      .map(_.toString)

  def wsHandler(submissionId: SubmissionId): Flow[Message, Message, NotUsed] =
    Flow[Message]
      .mapConcat(_ => Nil)
      .merge(producer(submissionId))
      .map(l => TextMessage(l.toString))

  //institutions/<lei>/filings/<period>/submissions/<seqNr>
  val submissionWsPath: Route = {
    path(
      "institutions" / Segment / "filings" / Segment / "submissions" / IntNumber) {
      (lei, period, seqNr) =>
        val submissionId = SubmissionId(lei, period, seqNr)
        handleWebSocketMessages(wsHandler(submissionId))
    }
  }

  def submissionWsRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          submissionWsPath
        }
      }
    }
  }
}
