package hmda.validation.dsl

trait RegexDsl {

  def validEmail: Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = {
      val emailRegEx = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
      val Email = emailRegEx.r
      Email.findFirstIn(_) match {
        case Some(_) => true
        case None => false
      }
    }

    override def failure: String = s"is not a valid email"
  }

}
