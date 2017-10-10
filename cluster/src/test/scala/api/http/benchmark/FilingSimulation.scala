package api.http.benchmark

import com.typesafe.config.ConfigFactory
import hmda.api.protocol.admin.WriteInstitutionProtocol
import hmda.model.institution.Institution
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.language.postfixOps
import spray.json._

class FilingSimulation extends Simulation with WriteInstitutionProtocol {

  val config = ConfigFactory.load()
  val host = config.getString("hmda.benchmark.host")
  val port = config.getInt("hmda.benchmark.port")
  val adminPort = config.getInt("hmda.benchmark.adminPort")
  val nrOfUsers = config.getInt("hmda.benchmark.nrOfUsers")
  val rampUpTime = config.getInt("hmda.benchmark.rampUpTime")

  val institutionIds = (1 to nrOfUsers).toList

  val httpProtocol = http
    .baseURL(s"http://$host:$port")
    .acceptHeader("text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
    .header("cfpb-hmda-username", "user")
    //.header("cfpb-hmda-institutions", institutionIds.mkString(","))
    .disableCaching

  object InstitutionScenario {

    def list(institutionId: String) = {
      exec(http("Institutions")
        .get("/institutions")
        .header("cfpb-hmda-institutions", institutionId)
        .check(status is 200))
    }

    def create(institutionId: String) = {
      exec(http("Create Institution")
        .post(s"http://$host:$adminPort/institutions")
        .body(StringBody(
          Institution.empty.copy(id = institutionId, activityYear = 2017).toJson.toString
        )).asJSON
        .check(status is 201))
    }

    def filings(institutionId: String) = {
      exec(http("List Filings")
        .get(s"/institutions/$institutionId/filings/2017")
        .header("cfpb-hmda-institutions", institutionId)
        .check(status is 200))
    }
  }

  val listInstitutions = scenario("List Institutions").exec(InstitutionScenario.list("4277"))
  val createInstitutions = scenario("Create Institutions").exec(InstitutionScenario.create("4277"))
  val listFilings = scenario("List Filings").exec(InstitutionScenario.filings("4277"))

  setUp(
    listInstitutions.inject(
      atOnceUsers(1)
    ).protocols(httpProtocol),
    createInstitutions.inject(atOnceUsers(1)).protocols(httpProtocol),
    listFilings.inject(atOnceUsers(1))
      .protocols(httpProtocol)
  )

}
