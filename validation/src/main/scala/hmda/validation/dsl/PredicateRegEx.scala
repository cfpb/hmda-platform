package hmda.validation.dsl

import scala.language.implicitConversions
import scala.util.matching.Regex

object PredicateRegEx {
  implicit def validCensusTractFormat: Predicate[String] = new Predicate[String] {

    override def validate: (String) => Boolean = {
      val censusTractRegEx = "^[0-9]{4}.[0-9]{2}$"
      val CensusTract = censusTractRegEx.r
      matches(CensusTract)
    }

    override def failure: String = s"census tract is not in valid format"

  }

  implicit def validEmail: Predicate[String] = new Predicate[String] {

    override def validate: (String) => Boolean = {
      val emailRegEx = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
      val Email = emailRegEx.r
      matches(Email)
    }

    override def failure: String = s"is not a valid email"
  }

  implicit def validPhoneNumber: Predicate[String] = new Predicate[String] {

    override def validate: (String) => Boolean = {
      val phoneNumberRegEx = "^\\d{3}-\\d{3}-\\d{4}$"
      val PhoneNumber = phoneNumberRegEx.r
      matches(PhoneNumber)
    }

    override def failure: String = s"is not a valid phone number"
  }

  implicit def validZipCode: Predicate[String] = new Predicate[String] {

    override def validate: (String) => Boolean = {
      val zipCodeRegex = "^\\d{5}(?:-\\d{4})?$".r
      matches(zipCodeRegex)
    }

    override def failure: String = s"is not a valid zip code"
  }

  implicit def validTaxId: Predicate[String] = new Predicate[String] {

    override def validate: (String) => Boolean = {
      val taxIdRegex = "^\\d{2}-\\d{7}$".r
      matches(taxIdRegex)
    }

    override def failure: String = s"is not a valid tax ID"
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
