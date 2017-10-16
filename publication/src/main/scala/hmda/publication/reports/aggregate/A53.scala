package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.publication.reports._
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

object A53 {

  def filters(lar: LoanApplicationRegisterQuery): Boolean = {
    (lar.propertyType == 1 || lar.propertyType == 2) &&
      (lar.purpose == 3)
  }

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    fipsCode: Int
  ): Future[A5X] = A5X.generate("A53", larSource, fipsCode, filters)

}
