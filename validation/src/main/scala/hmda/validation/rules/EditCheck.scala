package hmda.validation.rules

import hmda.validation.dsl.Result

abstract class EditCheck[-T] {

  def name: String

  def apply(input: T): Result
}
