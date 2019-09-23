package hmda.serialization.filing

import hmda.model.filing.FilingDetails
import hmda.persistence.serialization.filing.FilingMessage
import hmda.persistence.serialization.filing.filingdetails.FilingDetailsMessage
import hmda.serialization.filing.FilingProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._

object FilingDetailsProtobufConverter {

  def filingDetailsToProtobuf(filingDetails: FilingDetails): FilingDetailsMessage =
    FilingDetailsMessage(
      if (filingDetails.filing.isEmpty) None
      else Some(filingToProtobuf(filingDetails.filing)),
      filingDetails.submissions.map(s => submissionToProtobuf(s))
    )

  def filingDetailsFromProtobuf(filingDetailsMessage: FilingDetailsMessage): FilingDetails =
    FilingDetails(
      filing = filingFromProtobuf(filingDetailsMessage.filing.getOrElse(FilingMessage())),
      submissions = filingDetailsMessage.submissions
        .map(s => submissionFromProtobuf(s))
        .toList
    )
}
