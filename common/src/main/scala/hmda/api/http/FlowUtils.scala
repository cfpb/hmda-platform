package hmda.api.http

import org.apache.pekko.NotUsed
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.{ HttpMethods, HttpRequest, HttpResponse, Uri }
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.scaladsl.{ Flow, Framing }
import org.apache.pekko.util.ByteString

import scala.concurrent.{ ExecutionContext, Future }
// $COVERAGE-OFF$
object FlowUtils {
  def framing: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), maximumFrameLength = 65536, allowTruncation = true)

  def byte2StringFlow: Flow[ByteString, String, NotUsed] =
    Flow[ByteString].map(bs => bs.utf8String)

  def singleConnectionFlow(requestParallelism: Int = 1)(implicit system: ActorSystem): Flow[HttpRequest, HttpResponse, NotUsed] =
    Flow[HttpRequest]
      .mapAsync[HttpResponse](requestParallelism)(Http().singleRequest(_))

  def sendGetRequest(req: String, url: Uri)(implicit system: ActorSystem, ec: ExecutionContext): Future[String] = {
    val request = HttpRequest(HttpMethods.GET, uri = s"$url/$req")
    for {
      response <- Http().singleRequest(request)
      content  <- Unmarshal(response.entity).to[String]
    } yield content
  }
}
// $COVERAGE-ON$