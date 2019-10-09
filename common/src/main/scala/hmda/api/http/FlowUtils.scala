package hmda.api.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, HttpResponse, Uri }
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Framing }
import akka.util.ByteString

import scala.concurrent.ExecutionContext

trait FlowUtils {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  def parallelism: Int

  def framing: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), maximumFrameLength = 65536, allowTruncation = true)

  def byte2StringFlow: Flow[ByteString, String, NotUsed] =
    Flow[ByteString].map(bs => bs.utf8String)

  def singleConnectionFlow: Flow[HttpRequest, HttpResponse, NotUsed] =
    Flow[HttpRequest]
      .mapAsync[HttpResponse](parallelism)(request => {
        for {
          response <- Http().singleRequest(request)
        } yield response
      })

  def sendGetRequest(req: String, url: Uri) = {
    val request = HttpRequest(HttpMethods.GET, uri = s"$url/$req")
    for {
      response <- Http().singleRequest(request)
      content  <- Unmarshal(response.entity).to[String]
    } yield content
  }

}
