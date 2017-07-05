package hmda.publication.reports.disclosure

import java.util.Calendar
import hmda.publication.reports.ReportGenerators._
import org.scalacheck.Gen
import hmda.publication.reports.util.DateUtil._

object DisclosureReportGenerators {

  implicit def d51Gen: Gen[D51] = {
    for {
      respId <- Gen.alphaStr
      instName <- Gen.alphaStr
      year = Calendar.getInstance().get(Calendar.YEAR)
      reportDate = formatDate(Calendar.getInstance().toInstant)
      msa <- msaReportGen
      applicantIncomes <- Gen.listOf(applicantIncomeGen)
      total <- totalDispositionGen
    } yield D51(respId, instName, year, reportDate, msa, applicantIncomes, total)
  }

}
