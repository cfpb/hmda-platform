package hmda.api.http.public.lar

import akka.http.scaladsl.server.Directives.{complete, get, path}

trait PublicLarHttpApi {

  val modifiedLar =
    path("") {
      get {
        complete("")
      }
    }

}
