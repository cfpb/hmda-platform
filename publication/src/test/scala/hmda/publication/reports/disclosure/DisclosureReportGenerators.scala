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
      applicantIncomes <- Gen.listOfN(5, applicantIncomeGen)
      total <- totalDispositionGen
    } yield D51(respId, instName, year, msa, applicantIncomes, total, reportDate)
  }

  implicit def d53Gen: Gen[D53] = {
    for {
      respId <- Gen.alphaStr
      instName <- Gen.alphaStr
      year = Calendar.getInstance().get(Calendar.YEAR)
      reportDate = formatDate(Calendar.getInstance().toInstant)
      msa <- msaReportGen
      applicantIncomes <- Gen.listOfN(5, applicantIncomeGen)
      total <- totalDispositionGen
    } yield D53(respId, instName, year, msa, applicantIncomes, total, reportDate)
  }

}
