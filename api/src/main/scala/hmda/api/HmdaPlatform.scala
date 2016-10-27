package hmda.api

import akka.actor._
import hmda.api.HmdaApi._
import hmda.persistence.HmdaSupervisor._

object HmdaPlatform {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("hmda")
    createSupervisor(system)

    val api = system.actorOf(HmdaApi.props(), "hmda-api")
    api ! StartHttpApi

  }

}
