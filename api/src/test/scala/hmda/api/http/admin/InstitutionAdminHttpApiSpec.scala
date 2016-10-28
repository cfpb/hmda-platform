package hmda.api.http.admin

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit._

import scala.concurrent.duration._
import akka.util.{ ByteString, Timeout }
import hmda.api.model.{ ModelGenerators, Status }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.MalformedRequestContentRejection
import hmda.api.http.BaseHttpApi
import hmda.model.institution.Institution
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

    val newInstitution = institutionGen.sample.get

    "return OK for GET requests to the root path" in {
      Get() ~> routes("hmda-admin-api") ~> check {
        responseAs[Status].status mustBe "OK"
        responseAs[Status].service mustBe "hmda-admin-api"
      }
    }
    "create a new institution" in {
      val id = newInstitution.id
      val jsonRequest = ByteString(newInstitution.toJson.toString)
      val postRequest = createRequest(jsonRequest, HttpMethods.POST)
      postRequest ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.Created
        responseAs[Institution] mustBe newInstitution
      }
    }
    "return error when trying to upload bad entity" in {
      val badJson = "bad payload".toJson
      val jsonRequest = ByteString(badJson.toString)
      val postRequest = createRequest(jsonRequest, HttpMethods.POST)
      postRequest ~> institutionAdminRoutes ~> check {
        rejection mustBe a[MalformedRequestContentRejection]
      }
    }
    "modify existing institution" in {
      val updatedInstitution = newInstitution.copy(cra = true, name = "new name")
      val jsonRequest = ByteString(updatedInstitution.toJson.toString)
      val putRequest = createRequest(jsonRequest, HttpMethods.PUT)
      putRequest ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[Institution] mustBe updatedInstitution
      }

    }
    "return not found when modifying non existing institution" in {
      val i1 = institutionGen.sample.get
      val jsonRequest = ByteString(i1.toJson.toString)
      val putRequest = createRequest(jsonRequest, HttpMethods.PUT)
      putRequest ~> institutionAdminRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
  }

  private def createRequest(jsonRequest: ByteString, method: HttpMethod): HttpRequest = {
    HttpRequest(
      method,
      uri = "/institutions",
      entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
    )
  }
}
