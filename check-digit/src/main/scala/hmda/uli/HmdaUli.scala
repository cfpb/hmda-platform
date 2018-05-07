package hmda.uli

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

class HmdaUli extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
    | _    _ __  __ _____            _    _ _      _____
    | | |  | |  \/  |  __ \   /\     | |  | | |    |_   _|
    | | |__| | \  / | |  | | /  \    | |  | | |      | |
    | |  __  | |\/| | |  | |/ /\ \   | |  | | |      | |
    | | |  | | |  | | |__| / ____ \  | |__| | |____ _| |_
    | |_|  |_|_|  |_|_____/_/    \_\  \____/|______|_____|
  """.stripMargin)

  val config = ConfigFactory.load()

  val host = config.getString("hmda.uli.http.host")
  val port = config.getInt("hmda.uli.http.port")

}
