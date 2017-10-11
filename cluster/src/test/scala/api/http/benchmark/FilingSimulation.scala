package api.http.benchmark

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.language.postfixOps

class FilingSimulation extends Simulation {

  val config = ConfigFactory.load()
  val host = config.getString("hmda.benchmark.host")
  val port = config.getInt("hmda.benchmark.port")
  val adminPort = config.getInt("hmda.benchmark.adminPort")
  val nrOfUsers = config.getInt("hmda.benchmark.nrOfUsers")
  val rampUpTime = config.getInt("hmda.benchmark.rampUpTime")
  val feeder = csv(config.getString("hmda.benchmark.feederFile"))

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

  object Institutions {

    val hmdaFiling =
      feed(feeder)
        .exec(http("Search")
          .get("/institutions")
          .header("cfpb-hmda-institutions", "${institutionId}")
          .check(status is 200))
        .pause(1)
        .exec(http("List Filings")
          .get("/institutions/${institutionId}/filings/2017")
          .header("cfpb-hmda-institutions", "${institutionId}")
          .check(status is 200))

    def filings(institutionId: String) = {
      exec(http("List Filings")
        .get(s"/institutions/$institutionId/filings/2017")
        .header("cfpb-hmda-institutions", institutionId)
        .check(status is 200))
    }

  }

  val user = scenario("HMDA User")
    .exec(Institutions.hmdaFiling)

  setUp(
    user.inject(rampUsers(nrOfUsers) over (rampUpTime seconds)).protocols(httpProtocol)
  )

}
