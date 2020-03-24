package hmda.model.filing

import com.typesafe.config.ConfigFactory
import hmda.model.ResourceUtils._
import hmda.utils.YearUtils.Period

object EditDescriptionLookup {

  case class EditDescription(editName: String, description: String, affectedFields: List[String])

  val config = ConfigFactory.load()
  val editDescriptionFileName2018 =
    config.getString("hmda.filing.2018.edits.descriptions.filename")
  val editDescriptionFileName2019 =
    config.getString("hmda.filing.2019.edits.descriptions.filename")
  val editDescriptionFileName2020Quarter =
    config.getString("hmda.filing.2020Quarter.edits.descriptions.filename")
  def editDescriptionList(file: Iterable[String]): Iterable[EditDescription] =
    file
      .drop(1)
      .map { s =>
        val values             = s.split("\\|", -1).map(_.trim).toList
        val editName           = values(0)
        val editDetails        = values(1)
        val affectedDataFields = values(2).split(";").map(_.trim)
        EditDescription(editName, s""""$editDetails"""", affectedDataFields.toList)
      }

  def editDescriptionMap(file: Iterable[String]): Map[String, EditDescription] =
    editDescriptionList(file).map(e => (e.editName, e)).toMap

  val editDescriptionLines2018        = fileLines(s"/$editDescriptionFileName2018")
  val editDescriptionLines2019        = fileLines(s"/$editDescriptionFileName2019")
  val editDescriptionLines2020Quarter = fileLines(s"/$editDescriptionFileName2020Quarter")

  val editDescriptionMap2018        = editDescriptionMap(editDescriptionLines2018)
  val editDescriptionMap2019        = editDescriptionMap(editDescriptionLines2019)
  val editDescriptionMap2020Quarter = editDescriptionMap(editDescriptionLines2020Quarter)

  def mapForPeriod(period: Period): Map[String, EditDescription] =
    period match {
      case Period(2018, None)    => editDescriptionMap2018
      case Period(2019, None)    => editDescriptionMap2019
      case Period(2020, Some(_)) => editDescriptionMap2020Quarter
      case _                     => editDescriptionMap2019
    }

  def lookupDescription(editName: String, period: Period = Period(2018, None)): String =
    mapForPeriod(period)
      .getOrElse(editName, EditDescription("", "", List()))
      .description

  def lookupFields(editName: String, period: Period = Period(2018, None)): List[String] =
    mapForPeriod(period)
      .getOrElse(editName, EditDescription("", "", List()))
      .affectedFields

}
