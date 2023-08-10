package hmda.authService

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import hmda.authService.api.HmdaAuthApi
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

object HmdaAuth extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
         _   _               _            _         _   _     
        | | | |_ __ ___   __| | __ _     / \  _   _| |_| |__  
        | |_| | '_ ` _ \ / _` |/ _` |   / _ \| | | | __| '_ \ 
        |  _  | | | | | | (_| | (_| |  / ___ \ |_| | |_| | | |
        |_| |_|_| |_| |_|\__,_|\__,_| /_/   \_\__,_|\__|_| |_|
  """.stripMargin)

  implicit val classicSystem: ClassicActorSystem = ClassicActorSystem("hmda-auth-system")
  implicit val system: ActorSystem[_]            = classicSystem.toTyped
  implicit val materializer: Materializer        = Materializer(system)
  implicit val ec: ExecutionContext              = system.executionContext
  implicit val timeout: Timeout                  = Timeout(1.hour)

  ActorSystem[Nothing](HmdaAuthApi(), HmdaAuthApi.name)
}