package hmda.util.conversion

import java.text.SimpleDateFormat

trait ColumnDataFormatter {

  val timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def dateToString(option:Option[java.sql.Timestamp] ): String =
    if(option !=null) {
      val entryTime: String = timeFormat.format(option.getOrElse(new java.sql.Timestamp(0)))
      java.sql.Timestamp.valueOf(entryTime).toString
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
