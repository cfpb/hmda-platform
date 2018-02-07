package hmda.validation.dsl

import scala.util.matching.Regex

object PredicateRegEx {
  def validCensusTractFormat: Predicate[String] = stringMatching("^\\d{4}\\.\\d{2}$".r)

  def validEmail: Predicate[String] =
    stringMatching("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$".r)

  def validPhoneNumber: Predicate[String] = stringMatching("^\\d{3}-\\d{3}-\\d{4}$".r)

  def validZipCode: Predicate[String] = stringMatching("^\\d{5}(?:-\\d{4})?$".r)

  def validTaxId: Predicate[String] = stringMatching("^\\d{2}-\\d{7}$".r)

  def numericMatching(pattern: String): Predicate[String] = stringMatching(regExFor(pattern))

  def containsDigits: Predicate[String] = stringMatching("\\d{1}".r)

  def ssnFormat: Predicate[String] = numericMatching("NNN-NN-NNNN")

  private def regExFor(pattern: String): Regex = {
    val result = pattern.map {
      case 'N' => "\\d"
      case '.' => "\\."
      case '-' => "\\-"
    }
    result.mkString("^", "", "$").r
  }

  private def stringMatching(regEx: Regex): Predicate[String] = {
    regEx.findFirstIn(_: String) match {
      case Some(_) => true
      case None => false
    }
  }
}
