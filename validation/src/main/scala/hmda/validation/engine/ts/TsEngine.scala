package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.engine.ts.syntactical.TsSyntacticalEngine
import hmda.validation.engine.ts.validity.TsValidityEngine
import scalaz._
import Scalaz._
import scala.concurrent.Future

trait TsEngine extends TsSyntacticalEngine with TsValidityEngine {

  def validateTs(ts: TransmittalSheet): Future[TsValidation] = {
    val fSyntactical = checkSyntactical(ts)

    for {
      fs <- fSyntactical
    } yield {
      (
        checkValidity(ts)
        |@| fs
      )((_, _) => ts)
    }
  }

}
