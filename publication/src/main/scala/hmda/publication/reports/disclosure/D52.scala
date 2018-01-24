package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports._

import scala.concurrent.Future

object D52 extends DisclosureReport {

  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 1) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[String] = {

    D5X.generateD5X("D52", filters, larSource, fipsCode, institution)
  }
}
