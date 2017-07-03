package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.publication.reports._
import hmda.util.SourceUtils

import scala.concurrent.Future

object DisclosureReportGenerator extends SourceUtils {

  def generateReports[as: AS, mat: MAT, ec: EC](larSource: Source[LoanApplicationRegister, NotUsed]): Future[Unit] = {
    genD51Report(larSource)
  }



  // Table filters:
  // Loan Type 2,3,4
  // Property Type 1,2
  // Purpose of Loan 1
  private def genD51Report(larSource: Source[LoanApplicationRegister, NotUsed]): Future[Unit] = {

    val filtered = larSource.filter { lar =>
      (lar.loan.loanType == 2 || lar.loan.loanType == 3 || lar.loan.loanType == 4) &&
        (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
        (lar.loan.purpose == 1)
    }

    //Income statistics
    


    Future {
      ()
    }
  }


}
