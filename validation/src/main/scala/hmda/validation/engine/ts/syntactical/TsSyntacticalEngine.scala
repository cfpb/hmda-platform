package hmda.validation.engine.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.api.ts.TsValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.Syntactical
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.syntactical._

import scala.concurrent.Future
import scalaz.Scalaz._

trait TsSyntacticalEngine extends TsCommonEngine with ValidationApi with TsValidationApi {

  private def s100(t: TransmittalSheet): Future[TsValidation] = {
    S100(t, findYearProcessed).map { result =>
      convertResult(t, result, "S100", t.agencyCode + t.respondent.id, Syntactical)
    }
  }

  private def s013(t: TransmittalSheet): Future[TsValidation] = {
    S013(t, findTimestamp).map { result =>
      convertResult(t, result, "S013", t.agencyCode + t.respondent.id, Syntactical)
    }
  }

  def checkSyntactical(ts: TransmittalSheet, ctx: ValidationContext): Future[TsValidation] = {
    val checksToRun: List[EditCheck[TransmittalSheet]] = List(
      S010,
      S020,
      S025.inContext(ctx),
      S028
    )
    val checks = checksToRun.map(check(_, ts, ts.agencyCode + ts.respondent.id, Syntactical))

    val fs100 = s100(ts)
    val fs013 = s013(ts)

    for {
      f100 <- fs100
      f013 <- fs013
    } yield {
      (
        validateAll(checks, ts)
        |@| f100
        |@| f013
      )((_, _, _) => ts)
    }
  }

}
