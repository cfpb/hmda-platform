package hmda.publication.reports.national

import java.util.Calendar

import hmda.publication.reports.ReportGenerators._
import hmda.publication.reports.util.DateUtil._
import org.scalacheck.Gen

object NationalAggregateReportGenerators {
  implicit def n5XGen: Gen[N5X] = {
    for {
      total <- Gen.listOfN(4, dispositionGen)
      year = Calendar.getInstance().get(Calendar.YEAR)
      reportDate = formatDate(Calendar.getInstance().toInstant)
      applicantIncomes <- Gen.listOfN(5, applicantIncomeGen)
      table <- Gen.alphaStr
      description <- Gen.alphaStr
    } yield N5X(year, applicantIncomes, total, table, description, reportDate)
  }
}
