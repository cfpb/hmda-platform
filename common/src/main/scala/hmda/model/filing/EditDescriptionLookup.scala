package hmda.model.filing

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._

object EditDescriptionLookup {

  case class EditDescription(editName: String, description: String, affectedFields: List[String])

  val config = ConfigFactory.load()
  val editDescriptionFileName2018 =
    config.getString("hmda.filing.2018.edits.descriptions.filename")
  val editDescriptionFileName2019 =
    config.getString("hmda.filing.2019.edits.descriptions.filename")

  def editDescriptionList(file: Iterable[String]): Iterable[EditDescription] =
    file
      .drop(1)
      .map { s =>
        val values             = s.split("\\|", -1).map(_.trim).toList
        val editName           = values(0)
        val editDetails        = values(1)
        val affectedDataFields = values(2).split(";").map(_.trim)
        EditDescription(editName, editDetails, affectedDataFields.toList)
      }

  def editDescriptionMap(file: Iterable[String]): Map[String, EditDescription] =
    editDescriptionList(file).map(e => (e.editName, e)).toMap

  val editDescriptionLines2018 = fileLines(s"/$editDescriptionFileName2018")
  val editDescriptionLines2019 = fileLines(s"/$editDescriptionFileName2019")

  val editDescriptionMap2018 = editDescriptionMap(editDescriptionLines2018)
  val editDescriptionMap2019 = editDescriptionMap(editDescriptionLines2019)

  def mapForYear(period: String): Map[String, EditDescription] =
    period match {
      case "2018" => editDescriptionMap2018
      case "2019" => editDescriptionMap2019
      case _      => editDescriptionMap2018
    }

  def lookupDescription(editName: String, period: String = "2018"): String =
    mapForYear(period)
      .getOrElse(editName, EditDescription("", "", List()))
      .description

  //TODO (pass in the year from ValidationFlow)
  def lookupFields(editName: String, period: String = "2018"): List[String] =
    mapForYear(period)
      .getOrElse(editName, EditDescription("", "", List()))
      .affectedFields

}
