package hmda.validation.engine.ts

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.context.ValidationContext
import hmda.validation.engine.ts.quality.TsQualityEngine
import hmda.validation.engine.ts.syntactical.TsSyntacticalEngine
import hmda.validation.engine.ts.validity.TsValidityEngine

import scalaz._
import Scalaz._
import scala.concurrent.{ ExecutionContext, Future }

trait TsEngine extends TsSyntacticalEngine with TsValidityEngine with TsQualityEngine {

  def validateTs(ts: TransmittalSheet, ctx: ValidationContext)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): TsValidation = {
    (
      checkValidity(ts, ctx)
      |@| checkSyntactical(ts, ctx)
      |@| checkQuality(ts, ctx)
    )((_, _, _) => ts)
  }
}
