package hmda.validation.dsl

object HmdaDSL {

  sealed trait Predicate[T] {
    def validate: T => Boolean
    def failure: String
  }

  implicit class Validation[T](data: T) {

    def test(predicate: Predicate[T]): Unit = {
      require(predicate validate data, s"Value $data ${predicate.failure}")
    }

    def shouldBe(predicate: Predicate[T]): Unit = {
      test(predicate)
    }

    //TODO: return list of errors
    def validationErrors: List[String] = ???

  }

  def equalTo[T](rhs: T): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _ == rhs
    override def failure: String = s"not equal to $rhs"
  }

  def containedIn[T](list: List[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = list.contains(_)
    override def failure: String = s"is not contained in valid values domain"
  }

}
