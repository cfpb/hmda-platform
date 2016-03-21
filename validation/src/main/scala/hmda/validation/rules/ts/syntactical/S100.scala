package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ CommonDsl, Result }

import scala.concurrent.{ ExecutionContext, Future }

/*
 Activity year must = year being processed (i.e. = 2016)
 */
object S100 extends CommonDsl {

  def apply(ts: TransmittalSheet, year: Int): Result = {
    ts.activityYear is equalTo(year)
  }

  def apply(ts: TransmittalSheet, fyear: Future[Int])(implicit ec: ExecutionContext): Future[Result] = {
    fyear.map(year => ts.activityYear is equalTo(year))
  }

}
