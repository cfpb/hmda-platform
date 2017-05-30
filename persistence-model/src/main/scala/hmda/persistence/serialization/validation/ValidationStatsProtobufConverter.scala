package hmda.persistence.serialization.validation

import hmda.persistence.messages.events.validation.ValidationStatsEvents._
import hmda.persistence.model.serialization.SubmissionEvents.SubmissionIdMessage
import hmda.persistence.model.serialization.ValidationStatsEvents._
import hmda.persistence.serialization.submission.SubmissionProtobufConverter.{ submissionIdFromProtobuf, submissionIdToProtobuf }

object ValidationStatsProtobufConverter {

  def submissionSubmittedTotalsAddedToProtobuf(event: SubmissionSubmittedTotalsAdded): SubmissionSubmittedTotalsAddedMessage = {
    SubmissionSubmittedTotalsAddedMessage(
      total = event.total,
      id = Some(submissionIdToProtobuf(event.id))
    )
  }
  def submissionSubmittedTotalsAddedFromProtobuf(msg: SubmissionSubmittedTotalsAddedMessage): SubmissionSubmittedTotalsAdded = {
    SubmissionSubmittedTotalsAdded(
      total = msg.total,
      id = submissionIdFromProtobuf(msg.id.getOrElse(SubmissionIdMessage()))
    )
  }

  def submissionTaxIdAddedToProtobuf(event: SubmissionTaxIdAdded): SubmissionTaxIdAddedMessage = {
    SubmissionTaxIdAddedMessage(
      taxId = event.taxId,
      id = Some(submissionIdToProtobuf(event.id))
    )
  }
  def submissionTaxIdAddedFromProtobuf(msg: SubmissionTaxIdAddedMessage): SubmissionTaxIdAdded = {
    SubmissionTaxIdAdded(
      taxId = msg.taxId,
      id = submissionIdFromProtobuf(msg.id.getOrElse(SubmissionIdMessage()))
    )
  }

  def submissionMacroStatsAddedToProtobuf(event: SubmissionMacroStatsAdded): SubmissionMacroStatsAddedMessage = {
    SubmissionMacroStatsAddedMessage(
      id = Some(submissionIdToProtobuf(event.id)),
      total = event.total,
      q070 = event.q070,
      q070Sold = event.q070Sold,
      q071 = event.q071Lars,
      q071Sold = event.q071Sold,
      q072 = event.q072Lars,
      q072Sold = event.q072Sold,
      q075Ratio = event.q075Ratio,
      q076Ratio = event.q076Ratio
    )
  }
  def submissionMacroStatsAddedFromProtobuf(msg: SubmissionMacroStatsAddedMessage): SubmissionMacroStatsAdded = {
    SubmissionMacroStatsAdded(
      id = submissionIdFromProtobuf(msg.id.getOrElse(SubmissionIdMessage())),
      total = msg.total,
      q070 = msg.q070,
      q070Sold = msg.q070Sold,
      q071Lars = msg.q071,
      q071Sold = msg.q071Sold,
      q072Lars = msg.q072,
      q072Sold = msg.q072Sold,
      q075Ratio = msg.q075Ratio,
      q076Ratio = msg.q076Ratio
    )
  }
}
