package hmda.validation.dsl

import scala.language.implicitConversions
import scala.util.matching.Regex

object PredicateRegEx {

  implicit def validEmail: Predicate[String] =
    stringMatching("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$".r)

  implicit def validPhoneNumber: Predicate[String] = stringMatching("^\\d{3}-\\d{3}-\\d{4}$".r)

  implicit def validZipCode: Predicate[String] = stringMatching("^\\d{5}(?:-\\d{4})?$".r)

  implicit def validTaxId: Predicate[String] = stringMatching("^\\d{2}-\\d{7}$".r)

  def numericMatching(pattern: String): Predicate[String] = stringMatching(regExFor(pattern))

  private def regExFor(pattern: String): Regex = {
    val result = pattern.map {
      case 'N' => "\\d"
      case '.' => "\\."
    }
    result.mkString("^", "", "$").r
  }

  private def stringMatching(regEx: Regex): Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = {
      regEx.findFirstIn(_) match {
        case Some(_) => true
        case None => false
      }
    }

    override def failure: String = "does not match provided pattern"
  }
}
