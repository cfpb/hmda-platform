package hmda.publication.reports.national

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.publication.reports._
import hmda.publication.reports.aggregate.A53
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

object N53 {
  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegisterQuery, NotUsed]
  ): Future[N5X] = N5X.generate(A53, larSource)
}
