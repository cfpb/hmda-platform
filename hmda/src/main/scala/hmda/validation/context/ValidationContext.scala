package hmda.validation.context

import hmda.model.institution.Institution

case class ValidationContext(institition: Option[Institution] = None,
                             filingYear: Option[Int] = Some(2018))
