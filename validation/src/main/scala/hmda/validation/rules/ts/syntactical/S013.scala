package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import scala.concurrent.{ ExecutionContext, Future }

object S013 {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

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
