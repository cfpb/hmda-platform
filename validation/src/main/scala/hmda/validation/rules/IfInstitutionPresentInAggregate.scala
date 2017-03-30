package hmda.validation.rules

import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext

object IfInstitutionPresentInAggregate {
  def apply[A, B](ctx: ValidationContext)(constructor: (Institution, Int) => AggregateEditCheck[A, B]): AggregateEditCheck[A, B] = {
    ctx match {
      case ValidationContext(Some(institution), Some(year)) => constructor(institution, year)
      case _ => new EmptyAggregateEditCheck
    }
  }
}
