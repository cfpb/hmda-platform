package hmda.validation.context

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.utils.YearUtils.Period

case class ValidationContext(
  institution: Option[Institution] = None,
  filingPeriod: Option[Period] = Some(Period(2018, None)),
  ts: Option[TransmittalSheet] = None
)
