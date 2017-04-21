package hmda.validation.rules

import hmda.validation.dsl.{ Result, Success }
import hmda.validation._
import scala.concurrent.Future

class EmptyAggregateEditCheck[A, B] extends AggregateEditCheck[A, B] {
  override def name: String = "empty"

  override def apply[as: AS, mat: MAT, ec: EC](input: A): Future[Result] = Future(Success())
}
