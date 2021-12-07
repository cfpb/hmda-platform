package hmda.serialization.submission

import hmda.messages.submission.HmdaRawDataReplies.LinesAdded
import hmda.persistence.serialization.raw.data.replies.LinesAddedMessage

object HmdaRawDataRepliesProtobufConverter {
  def linesAddedToProtobuf(evt: LinesAdded): LinesAddedMessage =
    LinesAddedMessage(
      evt.lines.map(HmdaRawDataEventsProtobufConverter.lineAddedToProtobuf)
    )

  def linesAddedFromProtobuf(msg: LinesAddedMessage): LinesAdded =
    LinesAdded(
      msg.lines.map(HmdaRawDataEventsProtobufConverter.lineAddedFromProtobuf).toList
    )
}