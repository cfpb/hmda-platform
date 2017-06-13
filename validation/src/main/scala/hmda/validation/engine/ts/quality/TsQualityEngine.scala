package hmda.validation.engine.ts.quality

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.validation.Quality
import hmda.validation._
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.quality._

import scala.concurrent.Future

trait TsQualityEngine extends TsCommonEngine with ValidationApi {

  def checkQuality(ts: TransmittalSheet, ctx: ValidationContext): TsValidation = {
    val checks = List(
      Q020,
      Q033.inContext(ctx)
    ).map(check(_, ts, ts.errorId, Quality, true))

    validateAll(checks, ts)
  }

  def checkQualityAsync[as: AS, mat: MAT](ts: TransmittalSheet, ctx: ValidationContext): Future[TsValidation] = {
    val checks = List(
      Q012.inContext(ctx),
      Q130.inContext(ctx)
    ).map(edit => checkAsync(edit, ts, ts.errorId, Quality, true))

    val fChecks = Future.sequence(checks)

    fChecks.map(list => validateAll(list, ts))
  }
}
