package hmda.validation.engine

import cats.Semigroup
import cats.data.ValidatedNel
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.validation.{Syntactical, ValidationError, Validity}
import hmda.validation.api.ValidationApi
import hmda.validation.rules.ts.syntactical.S300
import hmda.validation.rules.ts.validity.{V600, V601, V602}

object TsEngine extends ValidationApi {

  implicit val sg = new Semigroup[TransmittalSheet] {
    override def combine(x: TransmittalSheet,
                         y: TransmittalSheet): TransmittalSheet = x
  }

  def validateTs(ts: TransmittalSheet): HmdaValidation[TransmittalSheet] = {
    val validations = Vector(
      checkSyntactical(ts),
      checkValidity(ts) //,
      //checkQuality(ts)
    )

    validations.par.reduceLeft(_ combine _)
  }

  def checkSyntactical(
      ts: TransmittalSheet): HmdaValidation[TransmittalSheet] = {
    val checksToRun = Vector(
      S300
    )

    val checks: List[ValidatedNel[ValidationError, TransmittalSheet]] =
      checksToRun.par.map(check(_, ts, ts.LEI, Syntactical)).toList

    checks.par.reduceLeft(_ combine _)

  }

  def checkValidity(ts: TransmittalSheet): HmdaValidation[TransmittalSheet] = {
    val checksToRun = Vector(
      V600,
      V601,
      V602
    )

    val checks = checksToRun.par.map(check(_, ts, ts.LEI, Validity)).toList

    checks.par.reduceLeft(_ combine _)

  }

  def checkQuality(ts: TransmittalSheet): HmdaValidation[TransmittalSheet] = ???

}
