package api.http.load

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.language.postfixOps

class FilingSimulation extends Simulation {

  val httpProtocol = http
    .baseURL("http://localhost:8080")
    .acceptHeader("text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
    .header("cfpb-hmda-username", "user")
    .header("cfpb-hmda-institutions", "0,1,2,3,4,5,243179,35057,6999998,6999999,4277")

  val filingScenario = scenario("HMDA Filing")
    .exec(http("GET Institutions")
      .get("/institutions")
      .check(
        status is 200
      ))

  setUp(filingScenario.inject(
    constantUsersPerSec(2) during (10 seconds)
  ).protocols(httpProtocol))
}
