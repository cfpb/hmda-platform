package hmda.data.browser.rest

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.models._
import hmda.data.browser.repositories.Statistic
import hmda.data.browser.services.BrowserService
import monix.eval.Task
import monix.execution.Scheduler
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, MustMatchers, OneInstancePerTest}

class RoutesSpec
    extends FunSpec
    with MustMatchers
    with ScalatestRouteTest
    with MockFactory
    with OneInstancePerTest {
  implicit val monixScheduler: Scheduler = Scheduler.global
  val service: BrowserService = mock[BrowserService]
  val router = Routes(service)

  describe("routes") {
    it("responds with aggregations when performing an aggregate query") {
      val stat = Statistic(10, 100)
      val aggregation = Aggregation(stat.count,
                                    stat.sum,
                                    Race.Asian,
                                    ActionTaken.LoansOriginated)
      (service
        .fetchAggregate(_: Seq[Race], _: Seq[ActionTaken]))
        .expects(Race.Asian :: Nil, ActionTaken.LoansOriginated :: Nil)
        .returns(Task(aggregation :: Nil))

      Get("/view/nationwide?actions_taken=1&races=Asian") ~> router ~> check {
        responseAs[AggregationResponse] mustBe AggregationResponse(
          Parameters(None,
                     None,
                     Race.Asian.entryName :: Nil,
                     ActionTaken.LoansOriginated.value :: Nil),
          List(
            Aggregation(stat.count,
                        stat.sum,
                        Race.Asian,
                        ActionTaken.LoansOriginated)
          )
        )
        response.status mustBe OK
      }
    }
  }
}
