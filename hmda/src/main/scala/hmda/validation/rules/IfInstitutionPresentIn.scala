package hmda.validation.rules

import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext

object IfInstitutionPresentIn {
  def apply[T](ctx: ValidationContext)(constructor: Institution => EditCheck[T]): EditCheck[T] =
    ctx.institution match {
      case Some(institution) => constructor(institution)
      case None              => new EmptyEditCheck
    }

}
