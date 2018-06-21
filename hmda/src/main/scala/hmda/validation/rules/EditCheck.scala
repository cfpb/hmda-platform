package hmda.validation.rules

import hmda.validation.dsl.ValidationResult

abstract class EditCheck[-A] {
  def name: String
  def apply(input: A): ValidationResult
  def parent: String = name
}
