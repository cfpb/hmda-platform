package hmda.validation.engine

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ Result, Success, Failure }
import hmda.validation.rules.syntactical.{ S020, S010 }

import scalaz._
import scalaz.Scalaz._

trait TsValidationEngine {

  type StringValidation[T] = Validation[String, T]

  def validate(ts: TransmittalSheet): StringValidation[TransmittalSheet] = {
    def s010(t: TransmittalSheet): StringValidation[TransmittalSheet] = {
      S010(t) match {
        case Success() => t.success
        case Failure(msg) => msg.failure
      }
    }
    def s020(t: TransmittalSheet): StringValidation[TransmittalSheet] = {
      S020(t) match {
        case Success() => t.success
        case Failure(msg) => msg.failure
      }
    }

    (s010(ts)
      |@| s020(ts))((_, _) => ts)
  }

}
