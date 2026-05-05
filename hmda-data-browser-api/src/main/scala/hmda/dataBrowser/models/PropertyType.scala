package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable
// $COVERAGE-OFF$
sealed abstract class PropertyType(override val entryName: String)
    extends EnumEntry

// $COVERAGE-OFF$
object PropertyType extends Enum[PropertyType] {
  val values: immutable.IndexedSeq[PropertyType] = findValues

  case object SingleFamily extends PropertyType("1")
  case object Manufactured extends PropertyType("2")
  case object Multifamily extends PropertyType("3")

  def validatePropertyType(
      rawPropertyType: Seq[String]): Either[Seq[String], Seq[PropertyType]] = {
    val potentialPropertyTypes = rawPropertyType.map(propertyType =>
      (propertyType, PropertyType.withNameInsensitiveOption(propertyType)))

    val isValidPropertyType = potentialPropertyTypes.map(_._2).forall(_.isDefined)

    if (isValidPropertyType) Right(potentialPropertyTypes.flatMap(_._2))
    else
      Left(potentialPropertyTypes.collect {
        case (input, None) => input
      })
  }
}

// $COVERAGE-OFF$

