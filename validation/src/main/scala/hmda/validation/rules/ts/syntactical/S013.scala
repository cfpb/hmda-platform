package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import scala.concurrent.{ ExecutionContext, Future }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S013 {

  def apply(ts: TransmittalSheet, timestamp: Long): Result = {
    val t = ts.timestamp
    t is greaterThan(timestamp)
  }

  def apply(ts: TransmittalSheet, fTimestamp: Future[Long])(implicit ec: ExecutionContext): Future[Result] = {
    val t = ts.timestamp
    fTimestamp.map(time => t is greaterThan(time))
  }

  def name: String = "S013"

}
