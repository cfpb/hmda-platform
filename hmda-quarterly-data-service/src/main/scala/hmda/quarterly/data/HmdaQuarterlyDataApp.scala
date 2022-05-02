package hmda.quarterly.data

import akka.actor.typed.ActorSystem
import hmda.quarterly.data.api.HmdaQuarterlyDataApi
import org.slf4j.LoggerFactory

object HmdaQuarterlyDataApp extends App {

  val log = LoggerFactory.getLogger(HmdaQuarterlyDataApi.name)

  log.info(
    """
      |quarterly data
    """.stripMargin
  )
  ActorSystem[Nothing](HmdaQuarterlyDataApi.main, HmdaQuarterlyDataApi.name)
}