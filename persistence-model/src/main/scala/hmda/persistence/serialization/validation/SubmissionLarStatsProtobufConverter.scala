package hmda.persistence.serialization.validation

import hmda.persistence.messages.events.validation.SubmissionLarStatsEvents.{ MacroStatsUpdated, SubmittedLarsUpdated }
import hmda.persistence.model.serialization.SubmissionLarStatsEvents._

object SubmissionLarStatsProtobufConverter {

  def submittedLarsUpdatedToProtobuf(event: SubmittedLarsUpdated): SubmittedLarsUpdatedMessage = {
    SubmittedLarsUpdatedMessage(
      totalSubmitted = event.totalSubmitted
    )
  }

  def submittedLarsUpdatedFromProtobuf(msg: SubmittedLarsUpdatedMessage): SubmittedLarsUpdated = {
    SubmittedLarsUpdated(
      totalSubmitted = msg.totalSubmitted
    )
  }

  def macroStatsUpdatedToProtobuf(event: MacroStatsUpdated): MacroStatsUpdatedMessage = {
    MacroStatsUpdatedMessage(
      totalValidated = event.totalValidated,
      q070Total = event.q070Total,
      q070Sold = event.q070Sold,
      q071Total = event.q071Total,
      q071Sold = event.q071Sold,
      q072Total = event.q072Total,
      q072Sold = event.q072Sold,
      q075Ratio = event.q075Ratio,
      q076Ratio = event.q076Ratio
    )
  }

  def macroStatsUpdatedFromProtobuf(msg: MacroStatsUpdatedMessage): MacroStatsUpdated = {
    MacroStatsUpdated(
      totalValidated = msg.totalValidated,
      q070Total = msg.q070Total,
      q070Sold = msg.q070Sold,
      q071Total = msg.q071Total,
      q071Sold = msg.q071Sold,
      q072Total = msg.q072Total,
      q072Sold = msg.q072Sold,
      q075Ratio = msg.q075Ratio,
      q076Ratio = msg.q076Ratio
    )
  }
}
