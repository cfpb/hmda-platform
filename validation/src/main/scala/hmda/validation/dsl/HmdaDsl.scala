package hmda.validation.dsl

import java.text.SimpleDateFormat

trait HmdaDsl {
  def validTimestampFormat[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case s: String =>
        checkDateFormat(s)
      case _ => false
    }
    override def failure: String = s"invalid timestamp format"
  }

  def checkDateFormat[T](s: String): Boolean = {
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
