package hmda.api

import akka.actor.ActorSystem

class HmdaAdminApi(val system: ActorSystem) {

  def main(args: Array[String]): Unit = {
    println(system.name)
  }

}
