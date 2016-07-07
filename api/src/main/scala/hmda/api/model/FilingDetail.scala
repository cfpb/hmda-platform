package hmda.api.model

import hmda.model.fi.{ Filing, Submission }

case class FilingDetail(
  filing: Filing,
  submissions: Seq[Submission]
)

