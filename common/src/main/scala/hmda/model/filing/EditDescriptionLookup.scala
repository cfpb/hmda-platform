package hmda.model.filing

import com.github.tototoshi.csv.CSVReader
import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._
import hmda.model.census.County
import scala.collection.immutable

object EditDescriptionLookup {

  case class EditDescription(editName: String,
                             description: String,
                             affectedFields: List[String])

  val config = ConfigFactory.load()
  val editDescriptionFileName =
    config.getString("hmda.filing.edits.descriptions.filename")

  val countyFileName = config.getString("hmda.county.countynames")

  val editDescriptionList = {
    val lines = fileLines(s"/$editDescriptionFileName")
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

  val countyList: Seq[County] = {
    val reader = CSVReader.open(resource(s"/$countyFileName", "UTF-8"))
    reader.toStream
      .drop(1)
      .map { field =>
        County(field(0),
               field(3),
               field(7),
               field(8),
               field(9).trim().toInt,
               field(10).trim().toInt)
      }
  }

  val countiesMap =
    countyList.map(e => (s"${e.stateCode}${e.countyCode}", e)).toMap

  def lookupCounty(stateCode: Int, countyCode: Int): County = {
    countiesMap.getOrElse(s"${stateCode}${countyCode}", County())
  }

  val editDescriptionMap: Map[String, EditDescription] =
    editDescriptionList.map(e => (e.editName, e)).toMap

  def lookupDescription(editName: String): String =
    editDescriptionMap
      .getOrElse(editName, EditDescription("", "", List()))
      .description

  def lookupFields(editName: String): List[String] =
    editDescriptionMap
      .getOrElse(editName, EditDescription("", "", List()))
      .affectedFields

}
