package hmda.publisher.api

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.publisher.scheduler.AllSchedulers
import hmda.publisher.scheduler.schedules.{Schedule, Schedules}

import scala.concurrent.ExecutionContext

private class DataPublisherHttpApi(
                                    schedulers: AllSchedulers
                                  )(implicit ec: ExecutionContext) {

  //trigger/<schedulername>
  private val triggerScheduler =
    path("trigger" / Segment) { schedulerName =>
      post {
        respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
          Schedules.withNameOption(schedulerName) match {
            case Some(schedule) =>
              triggerSchedule(schedule)
              complete(202 -> s"Schedule ${schedulerName} has been triggered")
            case None =>
              complete(404 -> s"Scheduler ${schedulerName} not found. Available: ${Schedules.values.map(_.entryName).mkString(", ")}")
          }
        }
      }
    }

  private def triggerSchedule(msg: Schedule): Unit = {
    import schedulers._
    val receiver = msg match {
      case Schedules.PanelScheduler2018        => panelScheduler
      case Schedules.PanelScheduler2019        => panelScheduler
      case Schedules.PanelScheduler2020        => panelScheduler
      case Schedules.PanelScheduler2021        => panelScheduler
      case Schedules.LarPublicScheduler2018    => larPublicScheduler
      case Schedules.LarPublicScheduler2019    => larPublicScheduler
      case Schedules.LarPublicScheduler2020    => larPublicScheduler
      case Schedules.LarScheduler2018          => larScheduler
      case Schedules.LarScheduler2019          => larScheduler
      case Schedules.LarScheduler2020          => larScheduler
      case Schedules.LarScheduler2021          => larScheduler
      case Schedules.LarSchedulerLoanLimit2019 => larScheduler
      case Schedules.LarSchedulerLoanLimit2020 => larScheduler
      case Schedules.LarSchedulerLoanLimit2021 => larScheduler
      case Schedules.LarSchedulerQuarterly2020 => larScheduler
      case Schedules.LarSchedulerQuarterly2021 => larScheduler
      case Schedules.LarSchedulerQuarterly2022 => larScheduler
      case Schedules.TsPublicScheduler2018     => tsPublicScheduler
      case Schedules.TsPublicScheduler2019     => tsPublicScheduler
      case Schedules.TsPublicScheduler2020     => tsPublicScheduler
      case Schedules.TsScheduler2018           => tsScheduler
      case Schedules.TsScheduler2019           => tsScheduler
      case Schedules.TsScheduler2020           => tsScheduler
      case Schedules.TsScheduler2021           => tsScheduler
      case Schedules.TsSchedulerQuarterly2020  => tsScheduler
      case Schedules.TsSchedulerQuarterly2021  => tsScheduler
      case Schedules.TsSchedulerQuarterly2022  => tsScheduler

    }
    receiver ! msg
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