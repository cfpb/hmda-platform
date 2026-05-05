package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class LienStatus(override val entryName: String)
    extends EnumEntry

object LienStatus extends Enum[LienStatus] {
  val values: immutable.IndexedSeq[LienStatus] = findValues

  case object SecuredByFirstLien extends LienStatus("1")
  case object SecuredBySubordinateLien extends LienStatus("2")

  def validateLienStatus(
      rawLienStatus: Seq[String]): Either[Seq[String], Seq[LienStatus]] = {
    val potentialLienStatuses = rawLienStatus.map(lienStatus =>
      (lienStatus, LienStatus.withNameInsensitiveOption(lienStatus)))

    val isValidLienStatus = potentialLienStatuses.map(_._2).forall(_.isDefined)

    if (isValidLienStatus) Right(potentialLienStatuses.flatMap(_._2))
    else
      Left(potentialLienStatuses.collect {
        case (input, None) => input
      })
  }
}
