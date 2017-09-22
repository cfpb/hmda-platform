package hmda.model.publication.reports

import enumeratum.{ Enum, EnumEntry }

sealed abstract class ReportTypeEnum extends EnumEntry

object ReportTypeEnum extends Enum[ReportTypeEnum] {

  val values = findValues

  val byName: Map[String, ReportTypeEnum] = Map(
    "disclosure" -> Disclosure,
    "aggregate" -> Aggregate,
    "national aggregate" -> NationalAggregate
  )

  case object Disclosure extends ReportTypeEnum
  case object Aggregate extends ReportTypeEnum
  case object NationalAggregate extends ReportTypeEnum
}
