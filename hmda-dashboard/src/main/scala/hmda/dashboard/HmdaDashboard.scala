package hmda.dashboard

import akka.actor.typed.ActorSystem
import hmda.dashboard.api.HmdaDashboardApi
import org.slf4j.LoggerFactory

object HmdaDashboard extends App {

  val log = LoggerFactory.getLogger("hmda-dashboard")
  log.info("Starting hmda-dashboard")

  log.info {
    """
      |__    __  .___  ___.  _______       ___          _______       ___           _______. __    __  .______     ______        ___      .______       _______
      |  |  |  | |   \/   | |       \     /   \        |       \     /   \         /       ||  |  |  | |   _  \   /  __  \      /   \     |   _  \     |       \
      |  |__|  | |  \  /  | |  .--.  |   /  ^  \       |  .--.  |   /  ^  \       |   (----`|  |__|  | |  |_)  | |  |  |  |    /  ^  \    |  |_)  |    |  .--.  |
      |   __   | |  |\/|  | |  |  |  |  /  /_\  \      |  |  |  |  /  /_\  \       \   \    |   __   | |   _  <  |  |  |  |   /  /_\  \   |      /     |  |  |  |
      |  |  |  | |  |  |  | |  '--'  | /  _____  \     |  '--'  | /  _____  \  .----)   |   |  |  |  | |  |_)  | |  `--'  |  /  _____  \  |  |\  \----.|  '--'  |
      |__|  |__| |__|  |__| |_______/ /__/     \__\    |_______/ /__/     \__\ |_______/    |__|  |__| |______/   \______/  /__/     \__\ | _| `._____||_______/


    """.stripMargin
  }

  ActorSystem[Nothing](HmdaDashboardApi(), HmdaDashboardApi.name)
}