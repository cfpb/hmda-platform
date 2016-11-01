package hmda.query

import akka.actor.{ ActorRef, ActorSystem }

import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.institutions.InstitutionQuery
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.Await

class HmdaQuerySupervisorSpec extends WordSpec with MustMatchers {

  val system = ActorSystem()

  val querySupervisor = system.actorOf(HmdaQuerySupervisor.props(), "query-supervisor")

  implicit val ec = system.dispatcher
  val timeout = 2.seconds
  implicit val akkaTimeout = Timeout(timeout)

  "The HMDA Query Supervisor" must {

    "Find or create institutions query" in {
      val path = "akka://default/user/query-supervisor/institutions-query"
      val fQueryInstitution = (querySupervisor ? FindActorByName(InstitutionQuery.name)).mapTo[ActorRef]
      val queryInstitution = Await.result(fQueryInstitution, timeout)
      queryInstitution.path.toString mustBe path
    }

    "terminate ActorSystem" in {
      Thread.sleep(2000)
      system.terminate()
      system.whenTerminated.map { isTerminated =>
        isTerminated mustBe true
      }
    }

  }

}
