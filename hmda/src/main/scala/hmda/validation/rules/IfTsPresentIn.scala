package hmda.validation.rules

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.context.ValidationContext

object IfTsPresentIn {
  def apply[T](ctx: ValidationContext)(constructor: TransmittalSheet => EditCheck[T]): EditCheck[T] =
    ctx.ts match {
      case Some(transmittalSheet) => constructor(transmittalSheet)
      case None                   => new EmptyEditCheck
    }

}
