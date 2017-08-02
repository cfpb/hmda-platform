package hmda.validation.context

import hmda.model.institution.Institution

case class ValidationContext(institution: Option[Institution], filingYear: Option[Int])
