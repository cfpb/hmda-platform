package hmda.dataBrowser.models

import enumeratum._

import scala.collection.immutable

sealed abstract class Sex(override val entryName: String) extends EnumEntry

object Sex extends Enum[Sex] {
  val values: immutable.IndexedSeq[Sex] = findValues

  case object Female extends Sex("Female")
  case object Male extends Sex("Male")
  case object Joint extends Sex("Joint")
  case object SexNotApplicable extends Sex("Sex Not Available")

  def validateSexes(rawSexes: Seq[String]): Either[Seq[String], Seq[Sex]] = {
    val potentialSexes =
      rawSexes.map(sex => (sex, Sex.withNameInsensitiveOption(sex)))
    val isSexesValid = potentialSexes.map(_._2).forall(_.isDefined)

    if (isSexesValid) Right(potentialSexes.flatMap(_._2))
    else
      Left(
        potentialSexes.collect {
          case (input, None) => input
        }
      )
  }
}
