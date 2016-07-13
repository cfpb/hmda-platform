package hmda.validation.dsl

import java.text.SimpleDateFormat
import scala.language.implicitConversions

object PredicateHmda {
  implicit def validTimestampFormat[T]: Predicate[T] = (_: T) match {
    case s: String =>
      checkDateFormat(s)
    case _ => false
  }

  implicit def checkDateFormat[T](s: String): Boolean = {
    try {
      val format = new SimpleDateFormat("yyyyMMddHHmm")
      format.setLenient(false)
      format.parse(s)
      true
    } catch {
      case e: Exception => false
    }
  }
}
