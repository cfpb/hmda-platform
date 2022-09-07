package hmda.api.http.directives
import java.time.LocalDate

import akka.http.scaladsl.model.StatusCodes.BadRequest
import hmda.util.Filer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import hmda.api.http.model.ErrorResponse
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

object CreateFilingAuthorization {
  private val config = ConfigFactory.load()
  private val rulesConfig =
    Filer.parse(config).fold(error => throw new RuntimeException(s"Failed to parse filing rules in HOCON: $error"), identity)
  private val check = Filer.check(rulesConfig) _

  def isFilingAllowed(year: Int, quarter: Option[String])(successful: Route): Route = extractMatchedPath { path =>
    // we use this only for month and day information (not time)
    val now = LocalDate.now()
    if (check(year, now.getDayOfYear, quarter)) successful
    // The following message is used by the Frontend, please notify devs when modifying this text. 
    else complete((BadRequest, ErrorResponse(BadRequest.intValue, "The provided year or quarter is no longer available for filing", path)))
  }

  def isQuarterlyYearAllowed(year: Int)(successful: Route): Route = extractMatchedPath { path =>
    if (Filer.checkQuarterlyYear(rulesConfig)(year)) successful
    else complete((BadRequest, ErrorResponse(BadRequest.intValue, "The provided year is not available", path)))
  }
}