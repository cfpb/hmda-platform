package hmda.calculator

import akka.actor.typed.ActorSystem
import org.slf4j.LoggerFactory
// $COVERAGE-OFF$
object HmdaRateSpread extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
               _____         _______ ______  _____ _____  _____  ______          _____
             ||  __ \     /\|__   __|  ____|/ ____|  __ \|  __ \|  ____|   /\   |  __ \
             || |__) |   /  \  | |  | |__  | (___ | |__) | |__) | |__     /  \  | |  | |
             ||  _  /   / /\ \ | |  |  __|  \___ \|  ___/|  _  /|  __|   / /\ \ | |  | |
             || | \ \  / ____ \| |  | |____ ____) | |    | | \ \| |____ / ____ \| |__| |
             ||_|  \_\/_/    \_\_|  |______|_____/|_|    |_|  \_\______/_/    \_\_____/
           """.stripMargin)

  ActorSystem[Nothing](Guardian(), Guardian.name)
}
// $COVERAGE-ON$