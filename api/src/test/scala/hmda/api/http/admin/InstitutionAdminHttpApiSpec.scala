package hmda.api.http.admin

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit._

import scala.concurrent.duration._
import akka.util.{ ByteString, Timeout }
import hmda.api.model.{ ModelGenerators, Status }
import hmda.api.model.admin.WriteInstitution
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.http.BaseHttpApi
import hmda.persistence.HmdaSupervisor
import spray.json._

class InstitutionAdminHttpApiSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with BaseHttpApi
    with InstitutionAdminHttpApi
    with ModelGenerators
    with BeforeAndAfterAll {

  override implicit val timeout: Timeout = Timeout(10.seconds)
  override val log: LoggingAdapter = NoLogging

  override def beforeAll(): Unit = {
    HmdaSupervisor.createSupervisor(system)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "Institution Admin Paths" must {
    implicit def default(implicit system: ActorSystem) = RouteTestTimeout(10.seconds)
    "return OK for GET requests to the root path" in {
      Get() ~> routes("hmda-admin-api") ~> check {
        responseAs[Status].status mustBe "OK"
        responseAs[Status].service mustBe "hmda-admin-api"
      }
    }
    "create a new institution" in {
      val newInstitution = institutionGen.sample.get
      val id = newInstitution.id
      val jsonRequest = ByteString(newInstitution.toJson.toString)
      val postRequest = HttpRequest(
        HttpMethods.POST,
        uri = "/institutions",
        entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
      )
      postRequest ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.Created
        responseAs[WriteInstitution] mustBe WriteInstitution(true, id)
      }
    }
  }
}
