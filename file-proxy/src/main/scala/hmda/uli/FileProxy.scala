package hmda.proxy

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem => ClassicActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import scala.concurrent.ExecutionContext
import hmda.proxy.api.http.HmdaProxyApi
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

object FileProxy extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
               _____ _ _        ____                      
              |  ___(_) | ___  |  _ \ _ __ _____  ___   _ 
              | |_  | | |/ _ \ | |_) | '__/ _ \ \/ / | | |
              |  _| | | |  __/ |  __/| | | (_) >  <| |_| |
              |_|   |_|_|\___| |_|   |_|  \___/_/\_\\__, |
                                                    |___/ 
  """.stripMargin)

  implicit val classicSystem: ClassicActorSystem = ClassicActorSystem("file-proxy-system")
  implicit val system: ActorSystem[_]            = classicSystem.toTyped
  implicit val materializer: Materializer        = Materializer(system)
  implicit val ec: ExecutionContext              = system.executionContext
  implicit val timeout: Timeout                  = Timeout(1.hour)

  ActorSystem[Nothing](HmdaProxyApi(), HmdaProxyApi.name)
}