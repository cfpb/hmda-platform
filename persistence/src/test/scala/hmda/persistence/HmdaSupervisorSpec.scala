package hmda.persistence

import akka.actor.ActorRef
import akka.testkit.TestProbe
import akka.util.Timeout
import akka.pattern.ask
import hmda.actor.test.ActorSpec
import hmda.persistence.HmdaSupervisor.FindActorByName
import hmda.persistence.processing.SingleLarValidation

import scala.concurrent.Await
import scala.concurrent.duration._

class HmdaSupervisorSpec extends ActorSpec {

  val supervisor = system.actorOf(HmdaSupervisor.props(), "supervisor")

  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(2.seconds)

  val probe = TestProbe()

  "The HMDA Supervisor" must {
    "find or create single LAR validation actor" in {
      val path = "akka://default/user/supervisor/larValidation"
      val validatorF = (supervisor ? FindActorByName(SingleLarValidation.name)).mapTo[ActorRef]
      val validator = Await.result(validatorF, 2.seconds)
      validator.path.toString mustBe path
      val validator2F = (supervisor ? FindActorByName(SingleLarValidation.name)).mapTo[ActorRef]
      val validator2 = Await.result(validator2F, 2.seconds)
      validator2.path.toString mustBe path
    }

  }

}
