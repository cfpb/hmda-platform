package hmda.validation.dsl

import scala.language.implicitConversions
import scala.util.matching.Regex

object PredicateRegEx {

  implicit def validEmail: Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = {
      val emailRegEx = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
      val Email = emailRegEx.r
      matches(Email)
    }

    override def failure: String = s"is not a valid email"
  }

  def numericMatching(pattern: String): Predicate[String] = new Predicate[String] {
    val regEx = regExFor(pattern).r
    override def validate: (String) => Boolean = matches(regEx)
    override def failure: String = s"does not match provided numeric format"
  }

  private def regExFor(pattern: String): String = {
    val result = pattern.map {
      case 'N' => "[0-9]"
      case '.' => "\\."
    }
    "^" + result.reduce(_ + _) + "$"
  }

  private def matches(regEx: Regex): (String) => Boolean = {
    regEx.findFirstIn(_) match {
      case Some(_) => true
      case None => false
    }
  }
}
