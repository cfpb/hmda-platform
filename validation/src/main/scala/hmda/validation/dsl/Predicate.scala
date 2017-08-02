package hmda.validation.dsl

trait Predicate[T] {
  def validate: T => Boolean
}

object Predicate {
  import scala.language.implicitConversions
  implicit def func2predicate[T](f: T => Boolean): Predicate[T] = new Predicate[T] {
    override def validate = f
  }
}
