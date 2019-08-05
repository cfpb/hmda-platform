package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class Ethnicity(override val entryName: String)
    extends EnumEntry

object Ethnicity extends Enum[Ethnicity] {
  val values: immutable.IndexedSeq[Ethnicity] = findValues

  case object FreeFormText extends Ethnicity("Free Form Text Only")
  case object EthnicityNotAvailable extends Ethnicity("Ethnicity Not Available")
  case object HispanicOrLatino extends Ethnicity("Hispanic or Latino")
  case object NotHispanicOrLatino extends Ethnicity("Not Hispanic or Latino")
  case object Joint extends Ethnicity("Joint")

  def validEthnicities(
      rawEthnicities: Seq[String]): Either[Seq[String], Seq[Ethnicity]] = {
    val potentialEthnicities =
      rawEthnicities.map(ethnicity =>
        (ethnicity, Ethnicity.withNameInsensitiveOption(ethnicity)))
    val isValidEthnicity = potentialEthnicities.map(_._2).forall(_.isDefined)

    if (isValidEthnicity) Right(potentialEthnicities.flatMap(_._2))
    else
      Left(
        potentialEthnicities.collect {
          case (input, None) => input
        }
      )
  }
}
