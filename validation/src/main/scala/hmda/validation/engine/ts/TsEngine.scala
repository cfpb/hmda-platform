package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.engine.ts.quality.TsQualityEngine
import hmda.validation.engine.ts.syntactical.TsSyntacticalEngine
import hmda.validation.engine.ts.validity.TsValidityEngine

import scalaz._
import Scalaz._
import scala.concurrent.Future

trait TsEngine extends TsSyntacticalEngine with TsValidityEngine with TsQualityEngine {

  def validateTs(ts: TransmittalSheet, institution: Option[Institution]): Future[TsValidation] = {
    val fSyntactical = checkSyntactical(ts, institution)

    for {
      fs <- fSyntactical
    } yield {
      (
        checkValidity(ts)
        |@| fs
        |@| checkQuality(ts)
      )((_, _, _) => ts)
    }
  }

}
