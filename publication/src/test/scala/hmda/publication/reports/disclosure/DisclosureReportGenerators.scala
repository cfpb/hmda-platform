package hmda.publication.reports.disclosure

import java.util.Calendar
import hmda.publication.reports.ReportGenerators._
import org.scalacheck.Gen

object DisclosureReportGenerators {

  def d5XGen: Gen[D5X] = {
    for {
      respId <- Gen.alphaStr
      instName <- Gen.alphaStr
      year = Calendar.getInstance().get(Calendar.YEAR)
      msa <- msaReportGen
      applicantIncomes <- Gen.listOfN(5, applicantIncomeGen)
      total <- totalDispositionGen
    } yield D5X(respId, instName, year, msa, applicantIncomes, total, "D5-X", "description")
  }

  def d51Gen: Gen[D51] = {
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

}
