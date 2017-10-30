package hmda.publication.reports.national

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.publication.reports._
import hmda.publication.reports.aggregate.A52

import scala.concurrent.Future

object N52 {
  def generate[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[N5X] =
    N5X.generateN5X(A52, larSource)
}
