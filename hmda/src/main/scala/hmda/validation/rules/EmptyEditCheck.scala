package hmda.validation.rules
import hmda.validation.dsl.{ ValidationResult, ValidationSuccess }

class EmptyEditCheck[T] extends EditCheck[T] {
  override def name: String = "empty"

  override def apply(input: T): ValidationResult = ValidationSuccess
}
