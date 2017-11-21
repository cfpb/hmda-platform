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
      total <- Gen.listOfN(4, dispositionGen)
      table <- Gen.alphaStr
      description <- Gen.alphaStr
    } yield D5X(respId, instName, year, msa, applicantIncomes, total, table, description)
  }

}
