package hmda.reporting

import org.slf4j.LoggerFactory

object HmdaReporting extends App {

  val log = LoggerFactory.getLogger("hmda-reporting")

  log.info(
    """
      | _____                       _   _                              _
      ||  __ \                     | | (_)                 /\         (_)
      || |__) |___ _ __   ___  _ __| |_ _ _ __   __ _     /  \   _ __  _
      ||  _  // _ \ '_ \ / _ \| '__| __| | '_ \ / _` |   / /\ \ | '_ \| |
      || | \ \  __/ |_) | (_) | |  | |_| | | | | (_| |  / ____ \| |_) | |
      ||_|  \_\___| .__/ \___/|_|   \__|_|_| |_|\__, | /_/    \_\ .__/|_|
      |           | |                            __/ |          | |
      |           |_|                           |___/           |_|
      |
    """.stripMargin)

}
