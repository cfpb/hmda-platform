package hmda.dataBrowser.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.dataBrowser.api.DataBrowserDirectives._
import hmda.dataBrowser.models.{ ActionTaken, ConstructionMethod }
import org.scalatest.{ Matchers, WordSpec }

class DataBrowserDirectivesSpec extends WordSpec with ScalatestRouteTest with Matchers {
  "DataBrowserDirectives" must {
    "extractFieldsForRawQueries extracts non-mandatory query parameters and reports errors when too many criteria are provided" in {
      val route: Route = get {
        extractFieldsForRawQueries { queries =>
          fail("extractFieldsForRawQueries should have rejected the request as there are way too many query parameters")
          complete(StatusCodes.OK)
        }
      }

      Get(
        "/?races=White&sexes=Female,Male&actions_taken=1&loan_types=1&loan_purposes=1,2&lien_statuses=1&construction_method=1&dwelling_categories=Multifamily:Site-Built&loan_products=Conventional:First%20Lien&total_units=1&ethnicities=Joint&states=CA,AK&counties=001"
      ) ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("provide-two-or-less-filter-criteria") shouldBe true
      }
    }

    "extractFieldsForRawQueries extracts non-mandatory query parameters correctly" in {
      val route: Route = get {
        extractFieldsForRawQueries { queries =>
          queries.map(_.name) shouldBe List(
            "races",
            "sexes"
          )
          complete(StatusCodes.OK)
        }
      }

      Get("/?races=White&sexes=Female,Male") ~> route ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "extractYearsAndMsaAndStateAndCountyAndLEIBrowserFields extract years, msamds, states, counties and LEI" in {
      val route: Route = get {
        extractYearsAndMsaAndStateAndCountyAndLEIBrowserFields { queries =>
          queries.map(_.name) shouldBe List("year", "msamd")
          complete(StatusCodes.OK)
        }
      }

      Get("/?years=2018&msamds=34980") ~> route ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "extractNationwideMandatoryYears" in {
      val route: Route = get {
        extractNationwideMandatoryYears { queries =>
          queries.map(_.name) shouldBe List("year")
          complete(StatusCodes.OK)
        }
      }

      Get("/?years=2018") ~> route ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "extractFieldsForCount" in {
      val route: Route = get {
        extractFieldsForCount { queries =>
          queries.map(_.name) shouldBe List("actions_taken", "construction_methods")
          complete(StatusCodes.OK)
        }
      }

      Get(
        s"/?actions_taken=${ActionTaken.values.map(_.entryName).mkString(",")}&construction_methods=${ConstructionMethod.values.map(_.entryName).mkString(",")}"
      ) ~> route ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }
  }

}