package hmda.publication.reports.disclosure

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.{  Disposition, MSAReport }
import hmda.publication.reports._
import hmda.util.SourceUtils

import scala.concurrent.Future
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.disclosure.DispositionGenerator._

class DisclosureReportGenerator extends SourceUtils {

  def generateReports[as: AS, mat: MAT, ec: EC](larSource: Source[LoanApplicationRegister, NotUsed]): Future[Unit] = {
    val d51 = genD51Report(larSource)
    Future {
      ()
    }
  }

  // Table filters:
  // Loan Type 2,3,4
  // Property Type 1,2
  // Purpose of Loan 1
  private def genD51Report[as: AS, mat: MAT, ec: EC](larSource: Source[LoanApplicationRegister, NotUsed]): Future[D51] = {

    val filtered = larSource.filter { lar =>
      (lar.loan.loanType == 2 || lar.loan.loanType == 3 || lar.loan.loanType == 4) &&
        (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
        (lar.loan.purpose == 1)
    }

    //Income statistics

    //val incomeValuesF = filtered
    //  .map(lar => lar.applicant.income)
    //  .filter(i => i != "NA")
    //  .runWith(Sink.seq)

    //incomeValuesF onComplete {
    //  case xs: Try[Seq[String]] => xs.getOrElse(Seq.empty).map(x => x.toInt)
    //}

    //val totalApplicationsReceived = filtered.filter(x => x)

    val dateF = filtered.take(1).map(lar => lar.actionTakenDate.toString.substring(0, 4).toInt)
    val totalF: Future[List[Disposition]] = calculateDispositions(filtered)

    for {
      date <- dateF
      total <- totalF
    } yield {
      D51(
        "",
        "",
        date,
        formatDate(Calendar.getInstance().toInstant),
        MSAReport("", "", "", ""),
        Nil,
        total
      )
    }

  }


}
