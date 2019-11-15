package hmda.api.http.directives

import java.time.LocalDate

import akka.http.scaladsl.model.StatusCodes.BadRequest
import hmda.util.Filer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import hmda.api.http.model.ErrorResponse
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

trait CreateFilingAuthorization {
  private val config = ConfigFactory.load()
  private val rulesConfig = Filer.parse(config).fold(error => throw new RuntimeException(s"Failed to parse filing rules in HOCON: $error"), identity)
  private val check = Filer.check(rulesConfig) _

  def createFilingAllowed(year: Int, quarter: Option[String])(successful: Route): Route = extractMatchedPath { path =>
    // we use this only for month and day information
    val now = LocalDate.now()
    val s = now.getDayOfYear
    if (check(year, now.getDayOfYear, quarter)) successful
    else complete(BadRequest, ErrorResponse(BadRequest.intValue, "The provided year or quarter isn't open at the moment", path))
  }
}