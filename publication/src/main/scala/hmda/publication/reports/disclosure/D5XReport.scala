package hmda.publication.reports.disclosure
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports.{ AS, EC, MAT }

import scala.concurrent.Future

trait D5XReport extends DisclosureReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean

  override def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution,
    msaList: List[Int]
  ): Future[DisclosureReportPayload] = {

    D5X.generateD5X(reportId, filters, larSource, fipsCode, institution)
  }
}

object D51 extends D5XReport {
  val reportId = "D51"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 2 || lar.loan.loanType == 3 || lar.loan.loanType == 4) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object D52 extends D5XReport {
  val reportId = "D52"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 1) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object D53 extends D5XReport {
  val reportId = "D53"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 3)
  }
}

object D54 extends D5XReport {
  val reportId = "D54"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 2)
  }
}

object D56 extends D5XReport {
  val reportId = "D56"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.occupancy == 2 &&
      (loan.propertyType == 1 || loan.propertyType == 2) &&
      (loan.purpose == 1 || loan.purpose == 2 || loan.purpose == 3)
  }
}

object D57 extends D5XReport {
  val reportId = "D57"
  def filters(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    loan.propertyType == 2 &&
      (loan.purpose == 1 || loan.purpose == 2 || loan.purpose == 3)
  }
}
