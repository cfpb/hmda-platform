package hmda.validation.rules

import hmda.validation.dsl.ValidationResult

import scala.concurrent.Future

abstract class AsyncEditCheck[-A] {
  def name: String
  def apply(input: A): Future[ValidationResult]
}
