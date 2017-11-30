package hmda.query

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.duration._

class HmdaQuerySupervisorSpec extends WordSpec with MustMatchers {

  val system = ActorSystem()

  implicit val ec = system.dispatcher
  val timeout = 2.seconds
  implicit val akkaTimeout = Timeout(timeout)

  "The HMDA Query Supervisor" must {

    "terminate ActorSystem" in {
      system.terminate()
      system.whenTerminated.map { isTerminated =>
        isTerminated mustBe true
      }
    }

  }

}
