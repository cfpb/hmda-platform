package hmda.dashboard.api

import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import hmda.dashboard.Settings
import hmda.dashboard.models._


trait DashboardDirectives extends Settings {

  private def extractDatetime: Directive1[String] =
    parameters("late_date" ).flatMap {
      case "" =>
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, "must provide late_date"))

      case x => provide(x)
    }

  def extractNationwideMandatoryYears(innerRoute: String => Route): Route =
    (extractDatetime) { (datetime) =>
      if (datetime.nonEmpty)
        innerRoute(datetime)
      else {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, ProvideDatetime()))
      }
    }

}

object DashboardDirectives extends DashboardDirectives