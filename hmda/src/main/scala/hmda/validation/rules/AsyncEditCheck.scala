package hmda.validation.rules

import hmda.validation.{ AS, EC, MAT }
import hmda.validation.dsl.ValidationResult

import scala.concurrent.Future

abstract class AsyncEditCheck[-A] {
  def name: String
  def apply[as: AS, mat: MAT, ec: EC](input: A): Future[ValidationResult]
}
