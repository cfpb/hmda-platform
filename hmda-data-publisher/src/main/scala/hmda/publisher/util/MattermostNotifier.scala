package hmda.publisher.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethods, HttpRequest}
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.syntax._

import scala.concurrent.Future
// $COVERAGE-OFF$
/**
 * curl -i -X POST -H 'Content-Type: application/json' -d '{"text": "Hello, this is some text\nThis is more text. :tada:"}' https://mattermost.goraft.tech/hooks/xxx
 * @param mattermostUrl
 */
class MattermostNotifier(mattermostUrl: String)(implicit as: ActorSystem) extends LazyLogging {
  import as.dispatcher

  def report(message: String): Future[Unit] =
    if (mattermostUrl.isEmpty) {
      logger.error("Reporting url is empty, messages won't be reported")
      Future.unit
    } else {
      val body = Json.obj("text" -> message.asJson).noSpaces
      Http()
        .singleRequest(
          HttpRequest(
            uri = mattermostUrl,
            method = HttpMethods.POST,
            entity = HttpEntity(body)
          )
        )
        .map(resp =>
          if (resp.status.isSuccess()) {
            ()
          } else {
            logger.error(s"Unexpected response from mattermost: ${resp}")
            throw new RuntimeException("Unexpected response from mattermost")
          }
        )
    }
}
// $COVERAGE-ON$