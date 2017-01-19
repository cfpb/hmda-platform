package hmda.api.http.public.lar

import akka.http.scaladsl.model.Uri.Path.Segment
import akka.http.scaladsl.server.Directives.{ complete, get, path }
import hmda.api.http.HmdaCustomDirectives

import scala.concurrent.ExecutionContext

trait PublicLarHttpApi extends HmdaCustomDirectives {

  def modifiedLar(institutionId: String)(implicit ec: ExecutionContext) =
    path("lar") {
      timedGet { uri =>
        complete("modified lar")
      }
    }

}
