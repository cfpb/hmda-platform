package hmda.validation.rules

import hmda.validation.dsl.{ Result, Success }

class EmptyEditCheck[T] extends EditCheck[T] {
  def name = "empty"
  def apply(input: T): Result = Success()
}
