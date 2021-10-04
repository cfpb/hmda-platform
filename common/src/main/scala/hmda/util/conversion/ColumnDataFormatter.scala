package hmda.util.conversion

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Date
import scala.util.Try

trait ColumnDataFormatter {

  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
  private val bomTypes = Seq("""\ufeff""", """ï»¿""", """\efbbbf""", """\ffe""");
  private val nullMarker = "NULL"

  def dateToString(option: Option[Long]): String =
    if (option != null) {
      val epochLong = new Date(option.getOrElse(0L))
      if (epochLong.getTime.equals(0L)){
        nullMarker
      } else {
        val entryTime = dateFormatter.format(epochLong.toInstant)
        entryTime
      }
    } else {
      nullMarker
    }

  def dateFromString(str: String): Option[Long] =
    if (str != nullMarker) {
      Try(Instant.from(dateFormatter.parse(str)))
        .map(Date.from)
        .map(_.getTime)
        .toOption
    } else {
      None
    }


  def extractOpt(option: Option[Any]): Any =
    option.getOrElse("")

  def controlCharacterFilter(value: String): String = {
    val chars = value.toCharArray
    val charsCount = chars.length
    var i = 0
    var j = 0 // the index where the next non-control character should be copied
    while (i < charsCount) {
      if (chars(i) >= ' ') {
        if (i != j) chars(j) = chars(i) // only copying if there were any non-control characters
        j += 1
      }

      i += 1
    }

    if (i != j) {
      // something has been removed
      new String(chars, 0, j)
    } else value
  }

  def removeTrailingLARPipe(value: String): String = {
    if (!value.isEmpty && value.count(_ == '|') == 110 && value.endsWith("|")) {
      value.init
    } else {
      value
    }
  }
  def removeTrailingTSPipe(value: String): String = {
    if (!value.isEmpty && value.count(_ == '|') == 15 && value.endsWith("|")) {
      value.init
    } else {
      value
    }
  }
  def removeBOM(value: String): String =
    if (!value.isEmpty && bomTypes.exists(value.contains(_)) ){
      bomTypes.foldLeft(value)((dataLine, str) => dataLine.replaceAll(str, ""))
    }
    else {
      value
    }

  def toBigDecimalString(value: String): String =
    if (validNum(value)) {
      BigDecimal(value).bigDecimal.toPlainString
    } else {
      value
    }

  // on conversion of institution names to CSV, we must wrap names that include commas to avoid parsing issues in double quotes
  def escapeCommas(value: String): String = (s""""$value"""")


  def validNum(str: String): Boolean =
    !throwsNFE(BigDecimal(str).bigDecimal.toPlainString)

  def throwsNFE(formatAttempt: => Any): Boolean =
    try { formatAttempt; false } catch { case _: NumberFormatException => true }

}