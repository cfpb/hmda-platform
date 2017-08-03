package hmda.validation.dsl

import java.text.SimpleDateFormat

import hmda.model.fi.ts.NameAndAddress

object PredicateHmda {
  def validTimestampFormat[T]: Predicate[T] = (_: T) match {
    case s: String =>
      checkDateFormat(s)
    case _ => false
  }

  private def checkDateFormat[T](s: String): Boolean = {
    try {
      val format = new SimpleDateFormat("yyyyMMddHHmm")
      format.setLenient(false)
      format.parse(s)
      true
    } catch {
      case e: Exception => false
    }
  }

  def completeNameAndAddress[T <: NameAndAddress]: Predicate[T] = (info: T) =>
    List(info.name, info.address, info.city, info.state, info.zipCode).forall(!_.isEmpty)

}
