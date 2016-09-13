package hmda.persistence

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout
import hmda.persistence.HmdaSupervisor.{ FindActorById, FindActorByName }
import hmda.persistence.institutions.FilingPersistence
import hmda.persistence.processing.SingleLarValidation
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.Await
import scala.concurrent.duration._

class HmdaSupervisorSpec extends WordSpec with MustMatchers {

  val system = ActorSystem()

  val supervisor = system.actorOf(HmdaSupervisor.props(), "supervisor")

  implicit val ec = system.dispatcher
  val timeout = 2.seconds
  implicit val akkaTimeout = Timeout(timeout)

  "The HMDA Supervisor" must {
    "find or create actor by name" in {
      val path = "akka://default/user/supervisor/larValidation"
      val validatorF = (supervisor ? FindActorByName(SingleLarValidation.name)).mapTo[ActorRef]
      val validator = Await.result(validatorF, timeout)
      validator.path.toString mustBe path

      val validator2F = (supervisor ? FindActorByName(SingleLarValidation.name)).mapTo[ActorRef]
      val validator2 = Await.result(validator2F, timeout)
      validator2.path.toString mustBe path
    }

    "find or create actor by id" in {
      val institutionId = "0"
      val path = s"akka://default/user/supervisor/Filings-$institutionId"
      val filingsF = (supervisor ? FindActorById(FilingPersistence.name, institutionId)).mapTo[ActorRef]
      val filings = Await.result(filingsF, timeout)
      filings.path.toString mustBe path

      val filings2F = (supervisor ? FindActorById(FilingPersistence.name, institutionId)).mapTo[ActorRef]
      val filings2 = Await.result(filings2F, timeout)
      filings2.path.toString mustBe path
    }

  }

}
