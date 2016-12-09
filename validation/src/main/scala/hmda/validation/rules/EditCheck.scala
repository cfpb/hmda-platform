package hmda.validation.rules

import hmda.model.fi.RecordField
import hmda.validation.dsl.Result

abstract class EditCheck[-T] {

  def name: String

  def fields(input: T): Map[RecordField, String]

  def apply(input: T): Result
}
