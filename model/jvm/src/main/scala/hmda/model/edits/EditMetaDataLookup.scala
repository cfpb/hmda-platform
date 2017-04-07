package hmda.model.edits

import hmda.model.ResourceUtils
import com.github.tototoshi.csv.CSVParser.parse

object EditMetaDataLookup extends ResourceUtils {
  val values: Seq[EditMetaData] = {
    val lines = resourceLines("/edit-metadata.txt")

    lines.drop(1).map { line =>
      val values = parse(line, '\\', ',', '"').getOrElse(List())
      val category = values(0)
      val editType = values(1)
      val editNumber = values(2)
      val editDescription = values(3)
      val userFriendlyEditDescription = values(4)
      val fieldNames: Seq[String] = values(5).split(";").map(_.trim).filter(_.nonEmpty)

      EditMetaData(
        editNumber,
        editDescription,
        userFriendlyEditDescription,
        fieldNames
      )

    }.toSeq
  }

  def forEdit(editName: String): EditMetaData = {
    values.find(e => e.editNumber == editName)
      .getOrElse(EditMetaData(editName, "description not found", "description not found", Seq()))
  }
}

case class EditMetaData(
  editNumber: String,
  editDescription: String,
  userFriendlyEditDescription: String,
  fieldNames: Seq[String]
)
