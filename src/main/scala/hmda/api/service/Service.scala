package hmda.api.service

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext
import com.typesafe.config.Config

trait Service {

  implicit val system: ActorSystem
  implicit val ec: ExecutionContext
  implicit val materializer: ActorMaterializer

  val log: LoggingAdapter
  def config: Config

}

