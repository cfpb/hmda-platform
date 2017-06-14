package hmda.apiModel.model

import hmda.model.fi.{ Filing, Submission }

case class FilingDetail(
  filing: Filing,
  submissions: Seq[Submission]
)

