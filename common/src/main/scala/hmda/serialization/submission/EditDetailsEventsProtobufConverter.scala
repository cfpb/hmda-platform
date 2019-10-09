package hmda.serialization.submission

import hmda.messages.submission.EditDetailsEvents.{ EditDetailsAdded, EditDetailsRowCounted }
import hmda.persistence.serialization.edit.details.EditDetailsMessage
import hmda.persistence.serialization.edit.details.events.{ EditDetailsAddedMessage, EditDetailsRowCountedMessage }
import hmda.serialization.submission.EditDetailsProtobufConverter._

object EditDetailsEventsProtobufConverter {

  def editDetailsAddedToProtobuf(evt: EditDetailsAdded): EditDetailsAddedMessage =
    EditDetailsAddedMessage(
      Some(editDetailsToProtobuf(evt.editDetails))
    )

  def editDetailsAddedFromProtobuf(msg: EditDetailsAddedMessage): EditDetailsAdded =
    EditDetailsAdded(
      editDetailsFromProtobuf(msg.editDetails.getOrElse(EditDetailsMessage()))
    )

  def editDetailsRowCountedToProtobuf(evt: EditDetailsRowCounted): EditDetailsRowCountedMessage =
    EditDetailsRowCountedMessage(
      evt.count
    )

  def editDetailsRowCountedFromProtobuf(msg: EditDetailsRowCountedMessage): EditDetailsRowCounted =
    EditDetailsRowCounted(
      msg.count
    )
}
