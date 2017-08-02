package hmda.publication.reports.util

import java.time.{ Instant, ZoneId }
import java.time.format.{ DateTimeFormatter, FormatStyle }
import java.util.Locale

object DateUtil {
  def formatDate(instant: Instant): String = {
    val formatter = DateTimeFormatter
      .ofLocalizedDateTime(FormatStyle.SHORT)
      .withLocale(Locale.US)
      .withZone(ZoneId.systemDefault)
    formatter.format(instant)
  }
}
