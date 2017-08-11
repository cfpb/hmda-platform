package hmda.validation.engine.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.validation.Syntactical
import hmda.validation.{ AS, MAT }
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.syntactical._

import scala.concurrent.Future

trait TsSyntacticalEngine extends TsCommonEngine with ValidationApi {
  def checkSyntactical(ts: TransmittalSheet, ctx: ValidationContext): TsValidation = {
    val checksToRun = List(
      S010,
      S020,
      S025.inContext(ctx),
      S028,
      S100.inContext(ctx)
    )
    val checks = checksToRun.map(check(_, ts, ts.errorId, Syntactical, true))

    validateAll(checks, ts)
  }

  def checkSyntacticalAsync[as: AS, mat: MAT](ts: TransmittalSheet, ctx: ValidationContext): Future[TsValidation] = {
    val checks = List(
      S011.inContext(ctx)
    ).map(edit => checkAsync(edit, ts, ts.errorId, Syntactical, true))

    val fChecks = Future.sequence(checks)

    fChecks.map(list => validateAll(list, ts))
  }

}
