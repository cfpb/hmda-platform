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
      val fieldNames: Seq[String] = values(5).split(";").map(_.trim)

      EditMetaData(
        editNumber,
        editDescription,
        userFriendlyEditDescription,
        fieldNames
      )

    }.toSeq
  }
}

case class EditMetaData(
  editNumber: String,
  editDescription: String,
  userFriendlyEditDescription: String,
  fieldNames: Seq[String]
)
