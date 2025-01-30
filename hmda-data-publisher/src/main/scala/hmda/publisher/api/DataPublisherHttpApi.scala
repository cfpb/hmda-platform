package hmda.publisher.api

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.publisher.scheduler.AllSchedulers
import hmda.publisher.scheduler.schedules.{ Schedule, ScheduleWithYear, Schedules }

private class DataPublisherHttpApi(schedulers: AllSchedulers) {

  //trigger/<schedulername>
  private val triggerScheduler =
    ignoreTrailingSlash {
      pathPrefix("trigger" / Segment) { schedulerName =>
        path(IntNumber) { year =>
          post {
            respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
              Schedules.withNameOption(schedulerName) match {
                case Some(schedulerName) =>
                  triggerSchedule(schedulerName, year)
                  complete(202 -> s"Schedule $schedulerName for $year has been triggered")
                case None =>
                  complete(404 -> s"Scheduler $schedulerName not found. Available: " +
                    Schedules.values.filterNot(s => s.entryName.matches("\\w+\\d{4}$")).map(_.entryName).mkString(", "))
              }
            }
          }
        }
      }
    }

  private def triggerSchedule(msg: Schedule, year: Int): Unit = {
    import schedulers._
    import Schedules._
    val receiver = msg match {
      case PanelSchedule => panelScheduler
      case LarPublicSchedule => larPublicScheduler
      case CombinedMLarPublicSchedule => combinedMLarPublicScheduler
      case LarSchedule => larScheduler
      case LarLoanLimitSchedule => larScheduler
      case LarQuarterlySchedule => larScheduler
      case TsPublicSchedule => tsPublicScheduler
      case TsSchedule => tsScheduler
      case TsQuarterlySchedule => tsScheduler
    }
    receiver ! ScheduleWithYear(msg, year)
  }

  def routes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          triggerScheduler
        }
      }
    }

}