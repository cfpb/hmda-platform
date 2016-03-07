package hmda.validation.rules.syntactical.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ CommonDsl, Result }

import scala.concurrent.{ ExecutionContext, Future }

object S013 extends CommonDsl {
  def apply(ts: TransmittalSheet, timestamp: Long): Result = {
    val t = ts.timestamp
    t is greaterThan(timestamp)
  }

  def apply(ts: TransmittalSheet, fTimestamp: Future[Long])(implicit ec: ExecutionContext): Future[Result] = {
    val t = ts.timestamp
    fTimestamp.map(time => t is greaterThan(time))
  }
}
