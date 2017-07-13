package hmda.publication

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Publication extends App {
  val log = LoggerFactory.getLogger("Publication")
  log.info("Starting publication subsystem")

  val config = ConfigFactory.load()

  implicit val system = ActorSystem(config.getString("clustering.name"))
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

}

