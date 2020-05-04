package hmda.dataBrowser

import akka.actor.typed.ActorSystem
import hmda.dataBrowser.api.DataBrowserApi
import org.slf4j.LoggerFactory

// $COVERAGE-OFF$
object DataBrowser extends App {

  val log = LoggerFactory.getLogger("data-browser")

  log.info(
    """
      | _    _ __  __ _____            _____        _          ____
      || |  | |  \/  |  __ \   /\     |  __ \      | |        |  _ \
      || |__| | \  / | |  | | /  \    | |  | | __ _| |_ __ _  | |_) |_ __ _____      _____  ___ _ __
      ||  __  | |\/| | |  | |/ /\ \   | |  | |/ _` | __/ _` | |  _ <| '__/ _ \ \ /\ / / __|/ _ \ '__|
      || |  | | |  | | |__| / ____ \  | |__| | (_| | || (_| | | |_) | | | (_) \ V  V /\__ \  __/ |
      ||_|  |_|_|  |_|_____/_/    \_\ |_____/ \__,_|\__\__,_| |____/|_|  \___/ \_/\_/ |___/\___|_|
      |
    """.stripMargin
  )

  ActorSystem[Nothing](DataBrowserApi(), DataBrowserApi.name)
}
// $COVERAGE-ON$