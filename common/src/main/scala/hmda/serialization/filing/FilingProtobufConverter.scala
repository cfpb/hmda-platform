package hmda.serialization.filing

import hmda.model.filing._
import hmda.persistence.serialization.filing.FilingMessage

object FilingProtobufConverter {

  def filingToProtobuf(filing: Filing): FilingMessage =
    FilingMessage(
      filing.period,
      filing.lei,
      filing.status.code,
      filing.filingRequired,
      filing.start,
      filing.end
    )

  def filingFromProtobuf(filingMessage: FilingMessage): Filing =
    Filing(
      filingMessage.period,
      filingMessage.lei,
      filingStatusFromInt(filingMessage.status),
      filingMessage.filingRequired,
      filingMessage.start,
      filingMessage.end
    )

  def filingStatusFromInt(code: Int): FilingStatus =
    code match {
      case 1  => NotStarted
      case 2  => InProgress
      case 3  => Completed
      case -1 => Cancelled
    }

}
