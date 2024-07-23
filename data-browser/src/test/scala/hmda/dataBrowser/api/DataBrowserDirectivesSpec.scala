package hmda.dataBrowser.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Source
import cats.implicits._
import hmda.dataBrowser.api.DataBrowserDirectives._
import hmda.dataBrowser.models._
import hmda.dataBrowser.services.{ FileService, QueryService }
import monix.eval.Task
import monix.execution.ExecutionModel
import monix.execution.schedulers.TestScheduler
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Assertion, Matchers, WordSpec }
import org.scalatest.Tag

object CustomTag extends Tag("actions-ignore")

class DataBrowserDirectivesSpec extends WordSpec with ScalatestRouteTest with Matchers with MockFactory with ScalaFutures {
  "DataBrowserDirectives" must {
    "obtainDataSource runs a query directly against the cache if the data is already present" in {
      implicit val scheduler: TestScheduler = TestScheduler(ExecutionModel.SynchronousExecution)
      val fileService                       = mock[FileService]
      val queryService                      = mock[QueryService]
      val url                               = "some.s3-url.com"
      (fileService.retrieveDataUrl _).expects(*, *, "2018").returns(Task.now(url.some))
      (queryService.fetchData _).expects(*).returns(Source.failed(new RuntimeException("should not pull")))

      val running = obtainDataSource(fileService, queryService)(QueryFields("2018", Nil), Commas, "2018").runToFuture
      scheduler.tick()
      whenReady(running)(_ shouldBe Right(url))
    }

    "extractFieldsForRawQueries extracts non-mandatory query parameters and reports errors when too many criteria are provided" in {
      val route: Route = failingRoute(extractFieldsForRawQueries("2018"))

      Get(
        "/?races=White&sexes=Female,Male&actions_taken=1&loan_types=1&loan_purposes=1,2&lien_statuses=1&construction_method=1&dwelling_categories=Multifamily:Site-Built&loan_products=Conventional:First%20Lien&total_units=1&ethnicities=Joint&states=CA,AK&counties=001"
      ) ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("provide-two-or-less-filter-criteria") shouldBe true
      }
    }

    "extractNonMandatoryQueryFields2018 prevents you from providing valid query parameters with invalid values" in {
      Get("/?construction_methods=999") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?loan_products=999") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?loan_purposes=999") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?loan_types=999") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?sexes=machine") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?total_units=wrong") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?races=rainbow") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?dwelling_categories=hut") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?lien_statuses=999") ~> failingRoute(extractNonMandatoryQueryFields("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?ethnicities=offworld") ~> Route.seal(failingRoute(extractNonMandatoryQueryFields("2018"))) ~> check {
        response.status shouldBe StatusCodes.NotFound
      }

      Get("/?actions_taken=99999") ~> Route.seal(failingRoute(extractNonMandatoryQueryFields("2018"))) ~> check {
        response.status shouldBe StatusCodes.NotFound
      }
    }

    "extractNonMandatoryQueryFields2018 (and primitive combinators) note that all query fields are present if you provide all possible query field values" in {
      val expectedTrue: QueryFields => Assertion = _.queryFields.forall(_.isAllSelected == true) shouldBe true

      Get(s"/?ethnicities=${Ethnicity.values.map(_.entryName).map(escape).mkString(",")}") ~> passingRoute(
        extractNonMandatoryQueryFields("2018")
      )(
        expectedTrue
      ) ~> check {
        response.status shouldBe StatusCodes.OK
      }

      Get(s"/?loan_types=${LoanType.values.map(_.entryName).map(escape).mkString(",")}") ~> passingRoute(
        extractNonMandatoryQueryFields("2018")
      )(
        expectedTrue
      ) ~> check {
        response.status shouldBe StatusCodes.OK
      }

      Get(s"/?sexes=${Sex.values.map(_.entryName).map(escape).mkString(",")}") ~> passingRoute(
        extractNonMandatoryQueryFields("2018")
      )(
        expectedTrue
      ) ~> check {
        response.status shouldBe StatusCodes.OK
      }

      Get(s"/?loan_purposes=${LoanPurpose.values.map(_.entryName).map(escape).mkString(",")}") ~> passingRoute(
        extractNonMandatoryQueryFields("2018")
      )(
        expectedTrue
      ) ~> check {
        response.status shouldBe StatusCodes.OK
      }

      Get(s"/?loan_products=${LoanProduct.values.map(_.entryName).map(escape).mkString(",")}") ~> passingRoute(
        extractNonMandatoryQueryFields("2018")
      )(
        expectedTrue
      ) ~> check {
        response.status shouldBe StatusCodes.OK
      }

      Get(s"/?dwelling_categories=${DwellingCategory.values.map(_.entryName).map(escape).mkString(",")}") ~> passingRoute(
        extractNonMandatoryQueryFields("2018")
      )(
        expectedTrue
      ) ~> check {
        response.status shouldBe StatusCodes.OK
      }

      Get(s"/?lien_statuses=${LienStatus.values.map(_.entryName).map(escape).mkString(",")}") ~> passingRoute(
        extractNonMandatoryQueryFields("2018")
      )(
        expectedTrue
      ) ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "extractFieldsForRawQueries extracts non-mandatory query parameters correctly" in {
      val route: Route = get {
        extractFieldsForRawQueries("2018") { queries =>
          queries.queryFields.map(_.name) shouldBe List(
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
        extractMsaAndStateAndCountyAndInstitutionIdentifierBrowserFields { queries =>
          queries.queryFields.map(_.name) shouldBe List("msamd")
          complete(StatusCodes.OK)
        }
      }

      Get("/?years=2018&msamds=34980") ~> route ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "extractYearsAndMsaAndStateAndCountyAndLEIBrowserFields prevents you from providing too many parameters" taggedAs CustomTag in {
      val route: Route = failingRoute(extractMsaAndStateAndCountyAndInstitutionIdentifierBrowserFields)

      Get("/?msamds=34980&leis=B90YWS6AFX2LGWOXJ1LD&states=CA,AK&actions_taken=1,2,3&counties=19125&years=2018") ~> route ~> check {
        responseAs[String].contains("provide-only-msamds-or-states-or-counties-or-leis") shouldBe true
        response.status shouldBe StatusCodes.BadRequest
      }
    }

    "extractYearsAndMsaAndStateAndCountyAndLEIBrowserFields prevents you from providing no parameters" in {
      val route: Route = failingRoute(extractMsaAndStateAndCountyAndInstitutionIdentifierBrowserFields)

      Get("/?years=2018") ~> route ~> check {
        responseAs[String].contains("provide-only-msamds-or-states-or-counties-or-leis") shouldBe true
        response.status shouldBe StatusCodes.BadRequest
      }
    }

    "extractYearsAndMsaAndStateAndCountyAndLEIBrowserFields must stop you if you don't provide enough mandatory parameters" in {
      val route: Route = failingRoute(extractMsaAndStateAndCountyAndInstitutionIdentifierBrowserFields)

      Get("/?years=2018") ~> route ~> check {
        responseAs[String].contains("provide-only-msamds-or-states-or-counties-or-leis") shouldBe true
        response.status shouldBe StatusCodes.BadRequest
      }
    }

    "extractNationwideMandatoryYears" in {
      val route: Route = get {
        extractNationwideMandatoryYears { queries =>
          queries.queryFields.map(_.name) shouldBe List("year")
          complete(StatusCodes.OK)
        }
      }

      Get("/?years=2018") ~> route ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "extractFieldsForCount should succeed when providing non mandatory fields" in {
      val route: Route = get {
        extractFieldsForCount("2018") { queries =>
          queries.queryFields.map(_.name) shouldBe List("actions_taken", "construction_methods")
          complete(StatusCodes.OK)
        }
      }

      Get(
        s"/?actions_taken=${ActionTaken.values.map(_.entryName).mkString(",")}&construction_methods=${ConstructionMethod.values.map(_.entryName).mkString(",")}"
      ) ~> route ~> check {
        response.status shouldBe StatusCodes.OK
      }
    }

    "extractFieldsForCount should complain when you provide no fields" in {
      Get("/") ~> failingRoute(extractFieldsForCount("2018")) ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("provide-atleast-one-filter-criteria") shouldBe true
      }
    }

    "extractCountFields should only extract mandatory fields" in {
      val route = get {
        extractCountFields(queries => complete(StatusCodes.OK))
      }

      Get("/?years=2017&msamds=34980&states=CA") ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }
    }

    "extractCountFields should prevent non-mandatory fields" in {
      val route = failingRoute(extractCountFields)

      Get("/?actions_taken=1,2,3&years=2018&msamds=34980&states=CA") ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("no-filters") shouldBe true
      }
    }

    "extractCountFields should prevent non-mandatory fields again" in {
      val route = failingRoute(extractCountFields)

      Get("/?actions_taken=1,2,3&years=2018&states=CA") ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("no-filters") shouldBe true
      }
    }

    "extractCountFields should make you provide fields" in {
      Get("/?years=2018") ~> failingRoute(extractCountFields) ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("provide-atleast-msamds-or-states") shouldBe true
      }
    }

    "extractNationwideMandatoryYears should complain if you don't provide years" in {
      val route = failingRoute(extractNationwideMandatoryYears)

      Get("/") ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("must provide years value parameter") shouldBe true
      }
    }

    "extractFieldsForAggregation should complain if you do not provide any fields" in {
      val route = failingRoute(extractFieldsForAggregation("2018"))

      Get("/") ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("provide-atleast-one-filter-criteria") shouldBe true
      }
    }

    "extractYearsMsaMdsStatesAndCounties should complain if you do not provide any fields" in {
      val route = failingRoute(extractYearsMsaMdsStatesAndCounties)

      Get("/") ~> route ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("must provide years value parameter") shouldBe true
      }
    }

    "extractYearsMsaMdsStatesAndCounties should fail if you provide it msamds, states and counties" in {
      Get("/?years=2018&msamds=1&states=CA&counties=19125") ~> failingRoute(extractYearsMsaMdsStatesAndCounties) ~> check {
        response.status shouldBe StatusCodes.BadRequest
        responseAs[String].contains("provide-only-msamds-or-states-or-counties-or-leis") shouldBe true
      }
    }

    "extractYearsMsaMdsStatesAndCounties should fail if you provide it msamds but invalid states or invalid counties" in {
      val route = failingRoute(extractYearsMsaMdsStatesAndCounties)
      // This is actually rejected but sealing it causes it to move and do a not found
      Get("/?states=ABCD&msamds=1&years=2018") ~> Route.seal(route) ~> check {
        response.status shouldBe StatusCodes.BadRequest
      }

      Get("/?counties=INVALID&years=2018") ~> Route.seal(route) ~> check {
        response.status shouldBe StatusCodes.NotFound
      }
    }
  }

  def passingRoute(inner: (QueryFields => Route) => Route)(expected: QueryFields => Assertion): Route = get {
    inner { queries =>
      expected(queries)
      complete(StatusCodes.OK)
    }
  }

  def escape(s: String): String =
    xml.Utility.escape(s).replace(" ", "%20")

  def failingRoute(inner: (QueryFields => Route) => Route): Route = get {
    inner { queries =>
      println(s"inner route accepted and produced ${queries}")
      fail(s"not supposed to get in here but produced")
      complete(StatusCodes.ImATeapot)
    }
  }
}