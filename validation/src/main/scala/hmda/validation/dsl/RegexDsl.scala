package hmda.validation.dsl

import scala.util.matching.Regex

trait RegexDsl {

  def validEmail: Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = {
      val emailRegEx = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
      val Email = emailRegEx.r
      matches(Email)
    }

    override def failure: String = s"is not a valid email"
  }

  def numericMatching: Predicate[String] = new Predicate[String] {
    val regEx = "^[0-9][0-9]\\.[0-9][0-9]$".r
    override def validate: (String) => Boolean = matches(regEx)
    override def failure: String = s"does not match provided numeric format"
  }

  private def matches(regEx: Regex): (String) => Boolean = {
    regEx.findFirstIn(_) match {
      case Some(_) => true
      case None => false
    }
  }
}
