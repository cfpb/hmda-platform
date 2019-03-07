package hmda.calculator

import org.slf4j.LoggerFactory

object RateSpread extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.debug(
    """
             | _____         _______ ______  _____ _____  _____  ______          _____
             ||  __ \     /\|__   __|  ____|/ ____|  __ \|  __ \|  ____|   /\   |  __ \
             || |__) |   /  \  | |  | |__  | (___ | |__) | |__) | |__     /  \  | |  | |
             ||  _  /   / /\ \ | |  |  __|  \___ \|  ___/|  _  /|  __|   / /\ \ | |  | |
             || | \ \  / ____ \| |  | |____ ____) | |    | | \ \| |____ / ____ \| |__| |
             ||_|  \_\/_/    \_\_|  |______|_____/|_|    |_|  \_\______/_/    \_\_____/                                      |_|
           """.stripMargin)

  log.debug("WTF")

  // implicit val system: ActorSystem = ActorSystem("hmda-census")
  // system.actorOf(HmdaCensusApi.props(), "hmda-census-api")
}
