package hmda.validation.context

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution

case class ValidationContext(institution: Option[Institution] = None,
                             filingYear: Option[Int] = Some(2018),
                             ts: Option[TransmittalSheet] = None)
