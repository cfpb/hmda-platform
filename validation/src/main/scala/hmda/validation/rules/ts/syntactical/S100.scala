package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result

import scala.concurrent.{ ExecutionContext, Future }
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

/*
 Activity year must = year being processed (i.e. = 2016)
 */
object S100 {

  def apply(ts: TransmittalSheet, year: Int): Result = {
    ts.activityYear is equalTo(year)
  }

  def apply(ts: TransmittalSheet, fyear: Future[Int])(implicit ec: ExecutionContext): Future[Result] = {
    fyear.map(year => ts.activityYear is equalTo(year))
  }

  def name: String = "S100"

}
