package hmda.util.conversion

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

trait ColumnDataFormatter {

  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

  def dateToString(option:Option[Long] ): String =
    if(option !=null) {

      val epochLong = new Date(option.getOrElse(0L) )
       val entryTime = dateFormatter.format(epochLong.toInstant)
      entryTime
    }else{
      "NA"
    }
  def extractOpt(option: Option[Any]): Any =
    option.getOrElse("")

  def controlCharacterFilter(value: String): String =
    if (!value.isEmpty) {
      value.filter(_ >= ' ').toString
    } else {
      value
    }
  def removeTrailingPipe(value: String): String =
    if (!value.isEmpty && value.endsWith("|")) {
      value.init
    } else {
      value
    }

  def removeBOM(value: String): String =
    if (!value.isEmpty){

      if (value.contains("""\ufeff"""))
      {
        value.replace("""\ufeff""", "")
      }
      else if (value.contains("""ï»¿"""))
      {
        value.replace("ï»¿", "")
      }
      else if (value.contains("""\efbbbf"""))
      {
        value.replace("""\efbbbf""", "")
      }
      else if (value.contains("""\ffe"""))
      {
        value.replace("""\ffe""", "")
      }
      else {
        value
      }
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
  def validNum(str: String): Boolean =
    !throwsNFE(BigDecimal(str).bigDecimal.toPlainString)

  def throwsNFE(formatAttempt: => Any): Boolean =
    try { formatAttempt; false } catch { case _: NumberFormatException => true }

}
