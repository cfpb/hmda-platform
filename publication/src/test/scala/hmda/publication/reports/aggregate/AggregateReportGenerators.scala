package hmda.publication.reports.aggregate

import java.util.Calendar

import hmda.publication.reports.ReportGenerators._
import hmda.publication.reports.util.DateUtil._
import org.scalacheck.Gen

object AggregateReportGenerators {
  implicit def a5XGen: Gen[A5X] = {
    for {
      msa <- msaReportGen
      year = Calendar.getInstance().get(Calendar.YEAR)
      reportDate = formatDate(Calendar.getInstance().toInstant)
      applicantIncomes <- Gen.listOfN(5, applicantIncomeGen)
      table <- Gen.alphaStr
      description <- Gen.alphaStr
      total <- totalDispositionGen
    } yield A5X(year, msa, applicantIncomes, total, table, description, reportDate)
  }
}
