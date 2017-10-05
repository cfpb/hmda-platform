package api.http.benchmark

import com.typesafe.config.ConfigFactory
import hmda.api.protocol.admin.WriteInstitutionProtocol
import hmda.model.institution.Institution
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps
import spray.json._
import hmda.model.institution.InstitutionGenerators._

class FilingSimulation extends Simulation with WriteInstitutionProtocol {

  val config = ConfigFactory.load()
  val host = config.getString("hmda.benchmark.host")
  val port = config.getInt("hmda.benchmark.port")
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
    .header("cfpb-hmda-institutions", institutionIds.mkString(","))
    .disableCaching

  val filingScenario = scenario("HMDA Filing")
    .exec(http("GET Institutions")
      .get("/institutions")
      .check(
        status is 200
      ))
  //.pause(1)
  //    .exec(http("POST Institutions")
  //      .post("/institutions")
  //      //.post(Institution.empty.toJson.toString()).asJSON
  //      .check(status is 201))

  setUp(filingScenario.inject(
    rampUsers(nrOfUsers) over (rampUpTime seconds)
  ).protocols(httpProtocol))
}
