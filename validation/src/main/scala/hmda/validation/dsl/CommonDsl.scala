package hmda.validation.dsl

trait CommonDsl {
  implicit class Rule[T](data: T) {
    private def test(predicate: Predicate[T]): Result = {
      predicate.validate(data) match {
        case true => Success()
        case false => Failure(predicate.failure)
      }
    }

    private def testNot(predicate: Predicate[T]): Result = {
      predicate.validate(data) match {
        case true => Failure(predicate.failure)
        case false => Success()
      }
    }

    def is(predicate: Predicate[T]): Result = {
      test(predicate)
    }

    def not(predicate: Predicate[T]): Result = {
      testNot(predicate)
    }

  }

  def equalTo[T](that: T): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _ == that
    override def failure: String = s"not equal to $that"
  }

  def greaterThan[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.gt(_, that)
    override def failure: String = s"not greater than $that"
  }

  def greaterThanOrEqual[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.gteq(_, that)
    override def failure: String = s"not greater than $that"
  }

  def lessThan[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.lt(_, that)
    override def failure: String = s"not greater than $that"
  }

  def lessThanOrEqual[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.lteq(_, that)
    override def failure: String = s"not greater than $that"
  }

  def containedIn[T](list: List[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = list.contains(_)
    override def failure: String = s"is not contained in valid values domain"
  }

  def numeric[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case n: Number => true
      case _ => false
    }
    override def failure: String = s"is not numeric"
  }

  def empty[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case s: String =>
        if (s.isEmpty) true else false
      case _ => false
    }
    override def failure: String = "is not empty"
  }

  def when(condition: Result, thenTest: => Result): Result = {
    condition match {
      case Success() => thenTest
      case Failure(_) => Success()
    }
  }

}
