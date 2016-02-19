package hmda.validation.dsl

import java.text.SimpleDateFormat

trait PlatformDsl {
  def validTimestampFormat[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case s: String =>
        try {
          val format = new SimpleDateFormat("yyyyMMddHHmm")
          format.setLenient(false)
          format.parse(s)
          true
        } catch {
          case e: Exception => false
        }
      case _ => false
    }
    override def failure: String = s"invalid timestamp format"
  }
}
