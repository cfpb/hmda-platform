package hmda.publication.reports.disclosure

import java.time.{ Instant, ZoneId }
import java.time.format.{ DateTimeFormatter, FormatStyle }
import java.util.{ Calendar, Locale }
import hmda.publication.reports.ReportGenerators._
import org.scalacheck.Gen

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

  private def formatDate(instant: Instant): String = {
    val formatter = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.SHORT)
      .withLocale(Locale.US)
      .withZone(ZoneId.systemDefault)
    formatter.format(instant)
  }

}
