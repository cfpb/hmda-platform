package hmda.validation.dsl

import scala.scalajs.js.Date

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
      val year = s.substring(0, 4)
      val d = Date.parse(s)
      d.asInstanceOf[AnyRef] match {
        case n: Number => true
        case _ => false
      }
    } catch {
      case e: Exception => false
    }
  }
}
