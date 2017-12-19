package hmda.validation.stats

import hmda.census.model.Msa
import hmda.model.fi.SubmissionId

case class SubmissionStats(
  id: SubmissionId,
  totalSubmittedLars: Int = 0,
  totalValidatedLars: Int = 0,
  q070Lars: Int = 0,
  q070SoldLars: Int = 0,
  q071Lars: Int = 0,
  q071SoldLars: Int = 0,
  q072Lars: Int = 0,
  q072SoldLars: Int = 0,
  q075Ratio: Double = 0.0,
  q076Ratio: Double = 0.0,
  taxId: String = "",
  msas: Seq[Msa] = Seq[Msa]()
)
