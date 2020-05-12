package hmda.publication

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.{ MustMatchers, WordSpecLike }

class HmdaPublicationSpec extends TestKit(ActorSystem("hmda-publication-spec")) with WordSpecLike with MustMatchers with ImplicitSender {
  "HmdaPublication" must {
    "respond to HmdaPublicationCommands" in {
      val actor = system.spawn(HmdaPublication(), HmdaPublication.name)
      watch(actor.toClassic)
      actor ! HmdaPublication.StopHmdaPublication
      expectTerminated(actor.toClassic)
    }
  }
}