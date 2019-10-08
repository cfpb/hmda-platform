package hmda.validation.dsl

import java.text.SimpleDateFormat

object PredicateHmda {

  def validDateFormat[T]: Predicate[T] = (_: T) match {
    case s: String =>
      checkDateFormat(s)
    case _ => false
  }

  private def checkDateFormat[T](s: String): Boolean =
    try {
      val format = new SimpleDateFormat("yyyyMMdd")
      format.setLenient(false)
      format.parse(s)
      s.length == 8
    } catch {
      case e: Exception => false
    }

}
