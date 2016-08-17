package hmda.validation.rules

import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext

object IfInstitutionPresentIn {
  def apply[T](ctx: ValidationContext)(constructor: (Institution) => EditCheck[T]): EditCheck[T] = {
    ctx.institution match {
      case Some(inst) => constructor(inst)
      case None => new EmptyEditCheck
    }
  }
}

