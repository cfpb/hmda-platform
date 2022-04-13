package hmda.proxy.api.http

import akka.NotUsed
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.uli.api.model.ULIModel._
import hmda.uli.api.model.ULIValidationErrorMessages.{invalidLoanIdLengthMessage, nonAlpanumericLoanIdMessage}
import hmda.uli.validation.ULI._
import hmda.util.http.FilingResponseUtils.failedResponse
import hmda.util.streams.FlowUtils._
import io.circe.generic.auto._
import org.slf4j.Logger

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object ProxyHttpApi {
  def create(log: Logger): Route = new ULIHttpApi(log).uliHttpRoutes
}
private class ProxyHttpApi(log: Logger) {
  val proxyHttpRoutes: Route =
    encodeResponse {
      path("file" / Segment) { s3Path =>
        (extractUri & get) { uri =>
        val s3File: Source[Option[(Source[ByteString, NotUsed], ObjectMetadata)], NotUsed] =
          S3.download("cfpb-hmda-public/prod" + s3path, bucketKey)

        val Some((data: Source[ByteString, _], metadata)) =
          s3File.runWith(Sink.head).futureValue:

        val result: Future[String] =
          data.map(_.utf8String).runWith(Sink.head)
        //#download

        result match {
          case Success(file) =>
            complete(ToResponseMarshallable(uli))
          case Failure(error) =>
            failedResponse(BadRequest, uri, error)
            }
          }
        }
      }
    }

}