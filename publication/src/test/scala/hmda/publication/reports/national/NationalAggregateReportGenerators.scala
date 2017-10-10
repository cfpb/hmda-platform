package hmda.publication.reports.national

import java.util.Calendar

import hmda.publication.reports.ReportGenerators._
import hmda.publication.reports.util.DateUtil._
import org.scalacheck.Gen

object NationalAggregateReportGenerators {
  implicit def n52Gen: Gen[N52] = {
    for {
      total <- totalDispositionGen
      year = Calendar.getInstance().get(Calendar.YEAR)
      reportDate = formatDate(Calendar.getInstance().toInstant)
      applicantIncomes <- Gen.listOfN(5, applicantIncomeGen)
    } yield N52(year, reportDate, applicantIncomes, total)
  }
}
