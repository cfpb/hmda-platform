package hmda.publisher.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import akka.util.Timeout
import hmda.publisher.scheduler.AllSchedulers
import hmda.publisher.scheduler.schedules.{ Schedule, ScheduleWithYear, Schedules }
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.duration.DurationInt

class DataPublisherHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest {
  implicit val timeout = Timeout(15.seconds)

  "DataPublisherHttpApi" must {
    "trigger right scheduler" in {
      Schedules.values.foreach({
        case Schedules.PanelScheduler2018            =>
        case x @ Schedules.PanelScheduler2019        => testTrigger(x, _.panelScheduler)
        case x @ Schedules.PanelScheduler2020        => testTrigger(x, _.panelScheduler)
        case x @ Schedules.PanelScheduler2020        => testTrigger(x, _.panelScheduler)
        case x @ Schedules.PanelScheduler2021        => testTrigger(x, _.panelScheduler)
        case x @ Schedules.PanelScheduler2022        => testTrigger(x, _.panelScheduler)
        case x @ Schedules.LarScheduler2018          => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarPublicScheduler2018    => testTrigger(x, _.larPublicScheduler)
        case x @ Schedules.LarPublicScheduler2019    => testTrigger(x, _.larPublicScheduler)
        case x @ Schedules.LarPublicScheduler2020    => testTrigger(x, _.larPublicScheduler)
        case x @ Schedules.LarPublicScheduler2021    => testTrigger(x, _.larPublicScheduler)
        case x @ Schedules.LarScheduler2019          => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarScheduler2020          => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarScheduler2021          => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarScheduler2022          => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarSchedulerLoanLimit2019 => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarSchedulerLoanLimit2020 => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarSchedulerLoanLimit2021 => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarSchedulerLoanLimit2022 => testTrigger(x, _.larScheduler)
        case x @ Schedules.TsScheduler2018           => testTrigger(x, _.tsScheduler)
        case x @ Schedules.TsPublicScheduler2018     => testTrigger(x, _.tsPublicScheduler)
        case x @ Schedules.TsPublicScheduler2019     => testTrigger(x, _.tsPublicScheduler)
        case x @ Schedules.TsPublicScheduler2020     => testTrigger(x, _.tsPublicScheduler)
        case x @ Schedules.TsPublicScheduler2021     => testTrigger(x, _.tsPublicScheduler)
        case x @ Schedules.TsScheduler2019           => testTrigger(x, _.tsScheduler)
        case x @ Schedules.TsScheduler2020           => testTrigger(x, _.tsScheduler)
        case x @ Schedules.TsScheduler2021           => testTrigger(x, _.tsScheduler)
        case x @ Schedules.TsScheduler2022           => testTrigger(x, _.tsScheduler)
        case x @ Schedules.LarSchedulerQuarterly2020 => testTrigger(x, _.larScheduler)
        case x @ Schedules.TsSchedulerQuarterly2020  => testTrigger(x, _.tsScheduler)
        case x @ Schedules.LarSchedulerQuarterly2021 => testTrigger(x, _.larScheduler)
        case x @ Schedules.TsSchedulerQuarterly2021  => testTrigger(x, _.tsScheduler)
        case x @ Schedules.LarSchedulerQuarterly2022 => testTrigger(x, _.larScheduler)
        case x @ Schedules.TsSchedulerQuarterly2022  => testTrigger(x, _.tsScheduler)
        case x @ Schedules.LarSchedulerQuarterly2023 => testTrigger(x, _.larScheduler)
        case x @ Schedules.TsSchedulerQuarterly2023  => testTrigger(x, _.tsScheduler)
        case x @ Schedules.PanelSchedule             => testTrigger(x, _.panelScheduler)
        case x @ Schedules.CombinedMLarPublicSchedule => testTrigger(x, _.combinedMLarPublicScheduler)
        case x @ Schedules.LarPublicSchedule         => testTrigger(x, _.larPublicScheduler)
        case x @ Schedules.LarSchedule               => testTrigger(x, _.larScheduler)
        case x @ Schedules.LarLoanLimitSchedule      => testTrigger(x, _.larScheduler)
        case x @ Schedules.TsPublicSchedule          => testTrigger(x, _.tsPublicScheduler)
        case x @ Schedules.TsSchedule                => testTrigger(x, _.tsScheduler)
        case x @ Schedules.LarQuarterlySchedule      => testTrigger(x, _.larScheduler)
        case x @ Schedules.TsQuarterlySchedule       => testTrigger(x, _.tsScheduler)
      })
    }
  }

  def testTrigger(msg: Schedule, schedulerToBeTriggered: AllSchedulers => ActorRef): Unit = {
    val probes @ List(p1, p2, p3, p4, p5,p6) = List.fill(6)(TestProbe())
    val allSchedulers = AllSchedulers(
      p1.ref,
      p2.ref,
      p3.ref,
      p4.ref,
      p5.ref,
      p6.ref
    )
    val routes    = new DataPublisherHttpApi(allSchedulers).routes
    val scheduler = schedulerToBeTriggered(allSchedulers)
    val probe     = probes.find(_.ref == scheduler).get
    val (scheduleWithYear, route) = msg match {
      case s if s.entryName.matches("\\w+\\d{4}$") => (false, s"/trigger/${s.entryName}")
      case s => (true, s"/trigger/${s.entryName}/2020")
    }
    Post(route) ~> routes ~> check {
      status mustBe StatusCodes.Accepted
    }
    probe.expectMsg(if (scheduleWithYear) ScheduleWithYear(msg, 2020) else msg)
  }

}