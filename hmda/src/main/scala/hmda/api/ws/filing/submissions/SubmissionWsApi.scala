package hmda.api.ws.filing.submissions

import java.time.Instant

import akka.{NotUsed, actor}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.model.ws.Message
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Source}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import com.typesafe.config.ConfigFactory
import hmda.api.ws.model.{
  KeepAliveWsResponse,
  ServerPing,
  SubmissionStatus,
  SubmissionStatusWSResponse
}
import io.circe.syntax._
import io.circe.generic.auto._
import hmda.model.filing.submission.SubmissionId
import hmda.messages.submission.SubmissionEvents.{
  SubmissionCreated,
  SubmissionEvent,
  SubmissionModified
}
import hmda.persistence.submission.SubmissionPersistence
import hmda.query.HmdaQuery._

import scala.concurrent.duration._

trait SubmissionWsApi {

  implicit val system: actor.ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  val configuration = ConfigFactory.load()
  val keepAliveTimeout = configuration.getInt("hmda.ws.keep-alive")

  def wsHandler(
      source: Source[String, NotUsed]): Flow[Message, Message, NotUsed] =
    Flow[Message]
      .mapConcat(_ => Nil) //ignore messages sent from client
      .merge(source)
      .map(l => TextMessage(l.toString))
      .keepAlive(keepAliveTimeout.seconds,
                 () => TextMessage(keepAliveResponse.asJson.noSpaces))

  //institutions/<lei>/filings/<period>/submissions/<seqNr>
  val submissionWsPath: Route = {
    path(
      "institutions" / Segment / "filings" / Segment / "submissions" / IntNumber) {
      (lei, period, seqNr) =>
        val submissionId = SubmissionId(lei, period, seqNr)
        val persistenceId = s"${SubmissionPersistence.name}-$submissionId"

        val source =
          eventEnvelopeByPersistenceId(persistenceId)
            .map(env => env.event.asInstanceOf[SubmissionEvent])
            .collect {
              case SubmissionCreated(submission)  => submission
              case SubmissionModified(submission) => submission
            }
            .map(
              s =>
                SubmissionStatusWSResponse(
                  s.status,
                  SubmissionStatus.messageType).asJson.noSpaces)

        handleWebSocketMessages(wsHandler(source))
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

  private def keepAliveResponse: KeepAliveWsResponse =
    KeepAliveWsResponse(Instant.now().toString, ServerPing.messageType)
}
