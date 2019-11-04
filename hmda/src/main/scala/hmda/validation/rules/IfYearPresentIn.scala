package hmda.validation.rules

import hmda.utils.YearUtils.Period
import hmda.validation.context.ValidationContext

object IfYearPresentIn {
  def apply[T](ctx: ValidationContext)(constructor: Int => EditCheck[T]): EditCheck[T] =
    ctx.filingPeriod match {
      case Some(Period(year, _)) => constructor(year)
      case None                  => new EmptyEditCheck
    }

}
