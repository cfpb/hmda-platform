package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class LienStatus2017(override val entryName: String)
    extends EnumEntry

object LienStatus2017 extends Enum[LienStatus] {
  val values: immutable.IndexedSeq[LienStatus] = findValues

  case object SecuredByFirstLien extends LienStatus2017("1")
  case object SecuredBySubordinateLien extends LienStatus2017("2")
  case object NotSecuredByLien extends LienStatus2017("3")
  case object NotApplicable extends LienStatus2017("4")

  def validateLienStatus2017(
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
