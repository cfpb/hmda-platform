package hmda.validation.rules.ts._2019

import hmda.model.filing.ts._2019.TransmittalSheet
import hmda.validation.context.ValidationContext
import hmda.validation.rules.{EditCheck, EmptyEditCheck}

object IfTsPresentIn {
  def apply[T](ctx: ValidationContext)(
      constructor: TransmittalSheet => EditCheck[T]): EditCheck[T] = {
    ctx.ts match {
      case Some(transmittalSheet) => constructor(transmittalSheet)
      case None                   => new EmptyEditCheck
    }
  }

}
