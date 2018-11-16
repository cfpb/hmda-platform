package hmda.model.filing

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._

object EditDescriptionLookup {

  case class EditDescription(editName: String, description: String, affectedFields: List[String])


  val config = ConfigFactory.load()
  val editDescriptionFileName =
    config.getString("hmda.filing.edits.descriptions.filename")

  val editDescriptionList = {
    val lines = fileLines(s"/$editDescriptionFileName")
    lines
      .drop(1)
      .map { line =>
        val values = line.split(",")
        val editName = values(0)
        val editDetails = values(1)
        val affectedDataFields = values(2).split(",")
        EditDescription(editName, editDetails, affectedDataFields.toList)
      }

  }


  val editDescriptionMap: Map[String, String] =
    editDescriptionList.map(e => (e.editName, e.description)).toMap

  def lookupDescription(editName: String): String =
    editDescriptionMap.getOrElse(editName, "")


}
