package hmda.validation.rules

import hmda.validation.dsl.{ CommonDsl, Result }

abstract class EditCheck[T] extends CommonDsl {

  def name: String

  def apply(lar: T): Result
}
