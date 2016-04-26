package hmda.validation.dsl

import scala.util.matching.Regex

trait RegexDsl {

  def validEmail: Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = {
      val emailRegEx = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
      val Email = emailRegEx.r
      checkRegEx(Email)
    }

    override def failure: String = s"is not a valid email"
  }

  def validCensusTractFormat: Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = {
      val censusTractRegEx = "^[0-9]{4}.[0-9]{2}$"
      val CensusTract = censusTractRegEx.r
      checkRegEx(CensusTract)
    }

    override def failure: String = s"census tract is not in valid format"

  }

  private def checkRegEx(regex: Regex): (String) => Boolean = {
    regex.findFirstIn(_) match {
      case Some(_) => true
      case None => false
    }
  }

}
