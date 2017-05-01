package hmda.validation.engine.ts.quality

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation._
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.Quality
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.quality._

import scala.concurrent.Future

trait TsQualityEngine extends TsCommonEngine with ValidationApi {

  def checkQuality[as: AS, mat: MAT, ec: EC](ts: TransmittalSheet, ctx: ValidationContext): Future[TsValidation] = {
    val tsId = ts.agencyCode + ts.respondent.id
    val checks = List(
      Q020,
      Q033.inContext(ctx)
    ).map(x => Future(check(x, ts, tsId, Quality, ts = true)))

    val allChecks = Future.sequence(checks :+ q012(ts, ctx))

    allChecks.map(c => validateAll(c, ts))
  }

  private def q012[as: AS, mat: MAT, ec: EC](ts: TransmittalSheet, ctx: ValidationContext) = {
    val fEdit = Q012.inContext(ctx)(ts)
    for {
      n <- fEdit
    } yield convertResult(ts, n, "Q012", "", Quality, ts = true)
  }
}
