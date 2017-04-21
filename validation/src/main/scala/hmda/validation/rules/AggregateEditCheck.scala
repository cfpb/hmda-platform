package hmda.validation.rules

import hmda.validation.dsl.Result
import hmda.validation._

import scala.concurrent.Future

abstract class AggregateEditCheck[-A, +B] extends SourceUtils {

  def name: String

  def apply[as: AS, mat: MAT, ec: EC](input: A): Future[Result]

}
