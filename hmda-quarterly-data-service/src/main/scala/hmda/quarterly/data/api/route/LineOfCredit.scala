package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import hmda.model.filing.lar.enums.{ LineOfCreditEnum, NotOpenEndLineOfCredit, OpenEndLineOfCredit }
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object LineOfCredit extends JsonSupport {
  private def getVolumeByType(lineOfCreditType: LineOfCreditEnum): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchLocVolumeByType(lineOfCreditType)
      .map(convertToGraph(lineOfCreditType.description, _))
      .runToFuture

  val routes: Route = pathPrefix("line_of_credit") {
    path("") {
      complete(for {
        open <- getVolumeByType(OpenEndLineOfCredit)
        closed <- getVolumeByType(NotOpenEndLineOfCredit)
      } yield Seq(open, closed))
    } ~ path("open") {
      complete(getVolumeByType(OpenEndLineOfCredit))
    } ~ path("not_open") {
      complete(getVolumeByType(NotOpenEndLineOfCredit))
    }
  }
}
