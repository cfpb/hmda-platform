package hmda.validation

import akka.actor.typed.scaladsl.adapter._
import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import org.scalatest.{ MustMatchers, WordSpecLike }

class HmdaValidationSpec extends TestKit(ActorSystem("hmda-validation-spec")) with WordSpecLike with MustMatchers with ImplicitSender {
  "HmdaValidation" must {
    "respond to HmdaValidationCommands" in {
      val actor = system.spawn(HmdaValidation(), HmdaValidation.name)
      watch(actor.toClassic)
      actor ! HmdaValidation.StopHmdaValidation
      expectTerminated(actor.toClassic)
    }
  }
}