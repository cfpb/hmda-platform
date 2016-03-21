package hmda.validation.dsl

trait Predicate[T] {
  def validate: T => Boolean
  def failure: String
}

