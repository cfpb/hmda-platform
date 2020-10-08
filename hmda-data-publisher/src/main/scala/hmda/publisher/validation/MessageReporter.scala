package hmda.publisher.validation

import akka.actor.ActorSystem
import akka.http.javadsl.model.HttpMethods
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.syntax._

import scala.concurrent.Future

/**
 * curl -i -X POST -H 'Content-Type: application/json' -d '{"text": "Hello, this is some text\nThis is more text. :tada:"}' https://mattermost.hook/stuff
 * @param mattermostUrl
 */
class MessageReporter(mattermostUrl: String)(implicit as: ActorSystem) extends LazyLogging {
  import as.dispatcher

  def report(message: String): Future[Unit] = {
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