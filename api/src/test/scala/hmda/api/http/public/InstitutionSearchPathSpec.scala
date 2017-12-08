package hmda.api.http.public

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri.Path
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.ErrorResponse
import hmda.api.model.public.InstitutionSearchResults
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import akka.pattern.ask
import hmda.model.institution.Institution
import hmda.persistence.demo.DemoData

class InstitutionSearchPathSpec extends InstitutionHttpApiSpec with ScalatestRouteTest with InstitutionSearchPaths {
  val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]

  "Institution Search" must {
    "return not found when email domain not present" in {
      Get("/institutions?domain=xxxxx") ~> institutionSearchPath(fInstitutionsActor) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, s"email domain xxxxx not found", Path("/institutions"))
      }
    }
    "return error when domain parameter is not present" in {
      Get("/institutions") ~> Route.seal(institutionSearchPath(fInstitutionsActor)) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[String] mustBe "Request is missing required query parameter 'domain'"
      }
    }
    "return filtered institution by email domain" in {
      Get("/institutions?domain=bank0.com") ~> institutionSearchPath(fInstitutionsActor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[InstitutionSearchResults].institutions.size mustBe 1
        responseAs[InstitutionSearchResults].institutions.head.id mustBe "0"
      }
    }
    "return institution filtered by valid id" in {
      Get("/institutions/0") ~> institutionSearchPath(fInstitutionsActor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe DemoData.testInstitutions.head
      }
    }
    "return 404 when filtering by invalid id" in {
      Get("/institutions/xxxx") ~> institutionSearchPath(fInstitutionsActor) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
  }

}
