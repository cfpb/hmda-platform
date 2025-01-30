package hmda.api.ws.filing.submissions

import java.time.Instant

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{ Flow, Source }
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import hmda.api.ws.model.{ KeepAliveWsResponse, ServerPing, SubmissionStatus, SubmissionStatusWSResponse }
import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionEvent, SubmissionModified }
import hmda.model.filing.submission.SubmissionId
import hmda.persistence.submission.SubmissionPersistence
import hmda.query.HmdaQuery._
import hmda.utils.YearUtils.Period
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.duration._

object SubmissionWsApi {
  def routes(implicit system: ActorSystem[_]): Route = {
    val config           = system.settings.config
    val keepAliveTimeout = config.getInt("hmda.ws.keep-alive")

    def wsHandler(source: Source[String, NotUsed]): Flow[Message, Message, NotUsed] =
      Flow[Message]
        .mapConcat(_ => Nil) //ignore messages sent from client
        .merge(source)
        .map(l => TextMessage(l.toString))
        .keepAlive(keepAliveTimeout.seconds, () => TextMessage(keepAliveResponse.asJson.noSpaces))

    def keepAliveResponse: KeepAliveWsResponse = KeepAliveWsResponse(Instant.now().toString, ServerPing.messageType)

    //institutions/<lei>/filings/<period>/submissions/<seqNr>
    val submissionRoute =
      path("institutions" / Segment / "filings" / Segment / "submissions" / IntNumber) { (lei, year, seqNr) =>
        val submissionId  = SubmissionId(lei, Period(year.toInt, None), seqNr)
        val persistenceId = s"${SubmissionPersistence.name}-$submissionId"

        val source =
          eventEnvelopeByPersistenceId(persistenceId)
            .map(env => env.event.asInstanceOf[SubmissionEvent])
            .collect {
              case SubmissionCreated(submission)  => submission
              case SubmissionModified(submission) => submission
            }
            .map(s => SubmissionStatusWSResponse(s.status, SubmissionStatus.messageType).asJson.noSpaces)

        handleWebSocketMessages(wsHandler(source))
      }

    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          submissionRoute
        }
      }
    }
  }
}