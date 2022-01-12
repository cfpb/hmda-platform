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
  val editDescriptionFileName2020 =
    config.getString("hmda.filing.2020.edits.descriptions.filename")
  val editDescriptionFileName2021Quarter =
    config.getString("hmda.filing.2021Quarter.edits.descriptions.filename")
  val editDescriptionFileName2021 =
    config.getString("hmda.filing.2021.edits.descriptions.filename")
  val editDescriptionFileName2022Quarter =
    config.getString("hmda.filing.2022Quarter.edits.descriptions.filename")
  val editDescriptionFileName2022 =
    config.getString("hmda.filing.2022.edits.descriptions.filename")

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
  val editDescriptionLines2020 = fileLines(s"/$editDescriptionFileName2020")
  val editDescriptionLines2021Quarter = fileLines(s"/$editDescriptionFileName2021Quarter")
  val editDescriptionLines2021 = fileLines(s"/$editDescriptionFileName2021")
  val editDescriptionLines2022Quarter = fileLines(s"/$editDescriptionFileName2022Quarter")
  val editDescriptionLines2022 = fileLines(s"/$editDescriptionFileName2022")


  val editDescriptionMap2018        = editDescriptionMap(editDescriptionLines2018)
  val editDescriptionMap2019        = editDescriptionMap(editDescriptionLines2019)
  val editDescriptionMap2020Quarter = editDescriptionMap(editDescriptionLines2020Quarter)
  val editDescriptionMap2020 = editDescriptionMap(editDescriptionLines2020)
  val editDescriptionMap2021Quarter = editDescriptionMap(editDescriptionLines2021Quarter)
  val editDescriptionMap2021 = editDescriptionMap(editDescriptionLines2021)
  val editDescriptionMap2022Quarter = editDescriptionMap(editDescriptionLines2022Quarter)
  val editDescriptionMap2022 = editDescriptionMap(editDescriptionLines2022)



  def mapForPeriod(period: Period): Map[String, EditDescription] =
    period match {
      case Period(2018, None)    => editDescriptionMap2018
      case Period(2019, None)    => editDescriptionMap2019
      case Period(2020, Some(_)) => editDescriptionMap2020Quarter
      case Period(2020, None)    => editDescriptionMap2020
      case Period(2021, Some(_)) => editDescriptionMap2021Quarter
      case Period(2021, None)    => editDescriptionMap2021
      case Period(2022, Some(_)) => editDescriptionMap2022Quarter
      case Period(2022, None) => editDescriptionMap2022
      case _                     => editDescriptionMap2021
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
