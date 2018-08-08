package hmda.institution

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.HmdaInstitutionQueryApi
import org.slf4j.LoggerFactory
import akka.actor.typed.scaladsl.adapter._
import hmda.institution.projection.InstitutionDBProjector
import hmda.institution.projection.InstitutionDBProjector.StartStreaming

object HmdaInstitutionApi extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      | _____          _   _ _         _   _                    ___  ______ _____
      ||_   _|        | | (_) |       | | (_)                  / _ \ | ___ \_   _|
      |  | | _ __  ___| |_ _| |_ _   _| |_ _  ___  _ __  ___  / /_\ \| |_/ / | |
      |  | || '_ \/ __| __| | __| | | | __| |/ _ \| '_ \/ __| |  _  ||  __/  | |
      | _| || | | \__ \ |_| | |_| |_| | |_| | (_) | | | \__ \ | | | || |    _| |_
      | \___/_| |_|___/\__|_|\__|\__,_|\__|_|\___/|_| |_|___/ \_| |_/\_|    \___/
    """.stripMargin)

  val config = ConfigFactory.load()

  val host = config.getString("hmda.institution.http.host")
  val port = config.getString("hmda.institution.http.port")

  implicit val system = ActorSystem("hmda-institutions")

  system.actorOf(HmdaInstitutionQueryApi.props(), "hmda-institutions-api")
  val institutionDBProjector = system.spawn(
    InstitutionDBProjector.streamMessages,
    InstitutionDBProjector.name)
  institutionDBProjector ! StartStreaming()

}
