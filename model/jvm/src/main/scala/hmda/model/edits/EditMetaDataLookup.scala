package hmda.model.edits

import hmda.model.ResourceUtils
import com.github.tototoshi.csv.CSVParser.parse

object EditMetaDataLookup extends ResourceUtils {
  val values: Seq[EditMetaData] = {
    val lines = resourceLines("/edit-metadata.txt", "iso-8859-1")

    lines.drop(1).map { line =>
      val values = parse(line, '\\', ',', '"').getOrElse(List())
      val category = values(0)
      val editType = values(1)
      val fieldNames: Seq[String] = values(2).split(";")
      val editNumber = values(3)
      val editDescription = values(4)
      val explanation = values(5)
      val userFriendlyEditDescription = values(6)

      EditMetaData(
        fieldNames,
        editNumber,
        editDescription,
        explanation,
        userFriendlyEditDescription
      )

    }.toSeq
  }
}

case class EditMetaData(
  fieldNames: Seq[String],
  editNumber: String,
  editDescription: String,
  explanation: String,
  userFriendlyEditDescription: String
)
