package hmda.model.publication.reports

import enumeratum.{ Enum, EnumEntry }

sealed abstract class ReportTypeEnum extends EnumEntry

object ReportTypeEnum extends Enum[ReportTypeEnum] {

  val values = findValues

  case object Disclosure extends ReportTypeEnum
  case object Aggregate extends ReportTypeEnum
  case object Nationwide extends ReportTypeEnum
}
