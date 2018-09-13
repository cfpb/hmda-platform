package hmda.validation.rules

import hmda.validation.context.ValidationContext

object IfYearPresentIn {
  def apply[T](ctx: ValidationContext)(
      constructor: Int => EditCheck[T]): EditCheck[T] = {
    ctx.filingYear match {
      case Some(year) => constructor(year)
      case None       => new EmptyEditCheck
    }
  }

}
