package hmda.validation.engine.ts.quality

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.Quality
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.quality._

import scala.concurrent.{ ExecutionContext, Future }

trait TsQualityEngine extends TsCommonEngine with ValidationApi {

  def checkQuality(ts: TransmittalSheet, ctx: ValidationContext)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[TsValidation] = {
    val tsId = ts.agencyCode + ts.respondent.id
    val checks = List(
      Q020,
      Q033.inContext(ctx)
    ).map(x => Future(check(x, ts, tsId, Quality, ts = true)))

    val allChecks = Future.sequence(checks :+ q012(ts, ctx) :+ q130(ts, ctx))

    allChecks.map(c => validateAll(c, ts))
  }

  private def q012(ts: TransmittalSheet, ctx: ValidationContext)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext) = {
    val fEdit = Q012.inContext(ctx)(ts)
    for {
      n <- fEdit
    } yield convertResult(ts, n, "Q012", "", Quality, ts = true)
  }

  private def q130(ts: TransmittalSheet, ctx: ValidationContext)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext) = {
    val fEdit = Q130.inContext(ctx)(ts)
    for {
      n <- fEdit
    } yield convertResult(ts, n, "Q130", "", Quality, ts = true)
  }
}
