package hmda.serialization.submission

import hmda.model.edits.{ EditDetails, EditDetailsRow, FieldDetails }
import hmda.persistence.serialization.edit.details.{ EditDetailsMessage, EditDetailsRowMessage, FieldDetailsMessage }

object EditDetailsProtobufConverter {

  def editDetailsToProtobuf(cmd: EditDetails): EditDetailsMessage =
    EditDetailsMessage(
      cmd.edit,
      cmd.rows.map(r => editDetailsRowToProtobuf(r))
    )

  def editDetailsFromProtobuf(msg: EditDetailsMessage): EditDetails =
    EditDetails(
      msg.edit,
      msg.rows.map(r => editDetailsRowFromProtobuf(r))
    )

  def editDetailsRowToProtobuf(cmd: EditDetailsRow): EditDetailsRowMessage =
    EditDetailsRowMessage(
      cmd.id,
      cmd.fields.map(f => fieldDetailsToProtobuf(f))
    )

  def editDetailsRowFromProtobuf(msg: EditDetailsRowMessage): EditDetailsRow =
    EditDetailsRow(
      msg.id,
      msg.fields.map(f => fieldDetailsFromProtobuf((f)))
    )

  def fieldDetailsToProtobuf(cmd: FieldDetails): FieldDetailsMessage =
    FieldDetailsMessage(
      cmd.name,
      cmd.value
    )

  def fieldDetailsFromProtobuf(msg: FieldDetailsMessage): FieldDetails =
    FieldDetails(
      msg.name,
      msg.value
    )

}
