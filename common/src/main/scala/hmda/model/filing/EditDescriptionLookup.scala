package hmda.model.filing

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._

import scala.collection.immutable

object EditDescriptionLookup {

  case class EditDescription(editName: String,
                             description: String,
                             affectedFields: List[String])

  val config = ConfigFactory.load()
  val editDescriptionFileName2018 =
    config.getString("hmda.filing.2018.edits.descriptions.filename")
  val editDescriptionFileName2019 =
    config.getString("hmda.filing.2019.edits.descriptions.filename")

  def fileForYear(period: String): hmda.Iterable[String] = {
    period match {
      case "2018" => fileLines(s"/$editDescriptionFileName2018")
      case "2019" => fileLines(s"/$editDescriptionFileName2019")
      case _ =>
        val currentYear = config.getString("hmda.filing.current")
        fileLines(
          config.getString(
            s"hmda.filing.$currentYear.edits.descriptions.filename"))
    }
  }

  def editDescriptionList(
      period: String): immutable.Iterable[EditDescription] = {
    val lines = fileForYear(period)
    lines
      .drop(1)
      .map { s =>
        val values = s.split("\\|", -1).map(_.trim).toList
        val editName = values(0)
        val editDetails = values(1)
        val affectedDataFields = values(2).split(";").map(_.trim)
        EditDescription(editName, editDetails, affectedDataFields.toList)
      }
  }

  def editDescriptionMap(period: String): Map[String, EditDescription] =
    editDescriptionList(period).map(e => (e.editName, e)).toMap

  def lookupDescription(editName: String, period: String): String =
    editDescriptionMap(period)
      .getOrElse(editName, EditDescription("", "", List()))
      .description

  //TODO (pass in the year from ValidationFlow)
  def lookupFields(editName: String, period: String = "2018"): List[String] =
    editDescriptionMap(period)
      .getOrElse(editName, EditDescription("", "", List()))
      .affectedFields

}
