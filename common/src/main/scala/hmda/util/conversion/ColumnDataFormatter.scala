package hmda.util.conversion

final case object ColumnDataFormatter {


  def controlCharacterFilter(value: String): String={
    if (!value.isEmpty){
      value.filter(_ >= ' ').toString
    }
    else{
      value
    }
  }
  def toBigDecimalString(value: String): String = {
    if (validNum(value)) {
      BigDecimal(value).bigDecimal.toPlainString
    } else {
      value
    }
  }
  def validNum(str: String): Boolean = {
    !throwsNFE(BigDecimal(str).bigDecimal.toPlainString)
  }

  def throwsNFE(formatAttempt: => Any): Boolean = {
    try { formatAttempt; false } catch { case _: NumberFormatException => true }
  }

}
