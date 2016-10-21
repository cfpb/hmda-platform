package hmda.api.http.admin

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

trait InstitutionAdminHttpApi {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  val institutionAdminRootPath =
    pathSingleSlash {
      complete("OK")
    }

  val institutionAdminPath = institutionAdminRootPath
}
