package hmda.model.filing

import hmda.model.filing.submission.Submission

case class FilingDetails(filing: Filing = Filing(),
                         submissions: List[Submission] = Nil)
