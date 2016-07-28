package hmda.validation.context

import hmda.model.institution.Institution

case class ValidationContext(filingYear: FilingYear, institution: Option[Institution])
