package hmda.validation.engine.ts.quality

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.Quality
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.quality._

import scala.concurrent.ExecutionContext

trait TsQualityEngine extends TsCommonEngine with ValidationApi {

  def checkQuality(ts: TransmittalSheet, ctx: ValidationContext)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): TsValidation = {
    val tsId = ts.agencyCode + ts.respondent.id
    val checks = List(
      Q020,
      Q033.inContext(ctx)
    ).map(check(_, ts, tsId, Quality, ts = true))

    validateAll(checks, ts)
  }

  private def q012(ts: TransmittalSheet, ctx: ValidationContext)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): EditCheck[TransmittalSheet] = {
    val fEdit = Q012.inContext(ctx)(ts)
    val result = for {
      n <- fEdit
    } yield convertResult(TransmittalSheet, n, "Q012", "", Quality, ts = true)
  }
}
