package hmda.serialization.submission

import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.persistence.serialization.raw.data.events.LineAddedMessage

object HmdaRawDataEventsProtobufConverter {

  def lineAddedToProtobuf(evt: LineAdded): LineAddedMessage = {
    LineAddedMessage(
      evt.timestamp,
      evt.data
    )
  }

  def lineAddedFromProtobuf(msg: LineAddedMessage): LineAdded = {
    LineAdded(
      msg.timestamp,
      msg.data
    )
  }

}
