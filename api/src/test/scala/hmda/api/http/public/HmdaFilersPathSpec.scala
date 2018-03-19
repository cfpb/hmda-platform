package hmda.api.http.public

import akka.actor.ActorRef
import akka.pattern.ask
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.CreateHmdaFiler
import hmda.model.institution.FilingGenerators._
import hmda.persistence.HmdaSupervisor
import hmda.persistence.HmdaSupervisor.FindHmdaFilerPersistence
import hmda.persistence.institutions.HmdaFilerPersistence
import hmda.validation.stats.ValidationStats
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.model.public.HmdaFilerResponse

class HmdaFilersPathSpec extends WordSpec with MustMatchers with BeforeAndAfterAll with ScalatestRouteTest with HmdaFilerPaths {

  val duration = 10.seconds

  override implicit val timeout: Timeout = Timeout(duration)
  override val ec: ExecutionContext = system.dispatcher
  override val log: LoggingAdapter = NoLogging

  val hmdaFiler1 = hmdaFilerGen.sample.get
  val hmdaFiler2 = hmdaFilerGen.sample.get

  val validationStats = system.actorOf(ValidationStats.props())
  val supervisor = system.actorOf(HmdaSupervisor.props(validationStats))

  val fHmdaFilerActor = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]

  override def beforeAll(): Unit = {
    super.beforeAll()
    val hmdaFilerPersistence = Await.result(fHmdaFilerActor, duration)
    hmdaFilerPersistence ! CreateHmdaFiler(hmdaFiler1)
    hmdaFilerPersistence ! CreateHmdaFiler(hmdaFiler2)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "HMDA Filer" must {
    "retrieve list of filers" in {
      Get("/filers") ~> hmdaFilersPath(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[HmdaFilerResponse] mustBe HmdaFilerResponse(Set(hmdaFiler1, hmdaFiler2))
      }
    }
  }

}
