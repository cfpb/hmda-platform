package hmda.dataBrowser.models

import enumeratum._

import scala.collection.immutable

sealed abstract class TotalUnits(override val entryName: String)
    extends EnumEntry

object TotalUnits extends Enum[TotalUnits] {
  val values: immutable.IndexedSeq[TotalUnits] = findValues

  case object One extends TotalUnits("1")
  case object Two extends TotalUnits("2")
  case object Three extends TotalUnits("3")
  case object Four extends TotalUnits("4")
  case object FiveToTwentyFour extends TotalUnits("5-24")
  case object TwentyFiveToFortyNine extends TotalUnits("25-49")
  case object FiftyToNinetyNine extends TotalUnits("50-99")
  case object HundredToFortyNine extends TotalUnits("100-149")
  case object GtHundredFortyNine extends TotalUnits(">149")

  def validateTotalUnits(
      rawTotalUnits: Seq[String]): Either[Seq[String], Seq[TotalUnits]] = {
    val potentialTotalUnits =
      rawTotalUnits.map(totalUnits =>
        (totalUnits, TotalUnits.withNameInsensitiveOption(totalUnits)))
    val isValidTotalUnit = potentialTotalUnits.map(_._2).forall(_.isDefined)

    if (isValidTotalUnit) Right(potentialTotalUnits.flatMap(_._2))
    else
      Left(
        potentialTotalUnits.collect {
          case (input, None) => input
        }
      )
  }
}
