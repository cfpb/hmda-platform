package hmda.publisher.util

import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class ScheduleCoordinatorSpec extends WordSpec with MustMatchers with ScalatestRouteTest {
  val scheduleCoordinator = ScheduleCoordinator
  "schedule coordinator " should {
    "have the correct name for its logger class" in {
      assert(scheduleCoordinator.getLogName == "hmda.publisher.util.ScheduleCoordinator$")

    }

    "have the correct command" in {
      assert(scheduleCoordinator.Command.Schedule.toString() == "Schedule")
    }

  }


}