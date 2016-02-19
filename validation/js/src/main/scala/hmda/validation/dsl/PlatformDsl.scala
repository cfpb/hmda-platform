package hmda.validation.dsl

import scala.scalajs.js.Date

trait PlatformDsl {
  //TODO: this function needs some review, doesn't seem to be doing its job properly
  def validTimestampFormat[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case s: String =>
        try {
          val d = Date.parse(s)
          println(d)
          d.asInstanceOf[AnyRef] match {
            case n: Number => true
            case _ => false
          }
        } catch {
          case e: Exception => false
        }
      case _ => false
    }
    override def failure: String = s"invalid timestamp format"
  }
}
