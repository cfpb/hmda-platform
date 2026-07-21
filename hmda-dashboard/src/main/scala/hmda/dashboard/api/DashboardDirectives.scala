package hmda.dashboard.api

import org.apache.pekko.http.scaladsl.model.StatusCodes.BadRequest
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server._
import org.apache.pekko.http.scaladsl.server.directives.RouteDirectives.complete
import hmda.dashboard.Settings
import hmda.dashboard.models._


trait DashboardDirectives extends Settings {

  private def extractDatetime: Directive1[String] =
    parameters("late_date" ).flatMap {
      case "" =>
        import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport._
        complete((BadRequest, "must provide late_date"))

      case x => provide(x)
    }

  def extractNationwideMandatoryYears(innerRoute: String => Route): Route =
    (extractDatetime) { (datetime) =>
      if (datetime.nonEmpty)
        innerRoute(datetime)
      else {
        import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport._
        complete((BadRequest, ProvideDatetime()))
      }
    }

}

object DashboardDirectives extends DashboardDirectives