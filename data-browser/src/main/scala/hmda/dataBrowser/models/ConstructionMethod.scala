package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class ConstructionMethod(override val entryName: String)
    extends EnumEntry

object ConstructionMethod extends Enum[ConstructionMethod] {
  val values: immutable.IndexedSeq[ConstructionMethod] = findValues

  case object SiteBuilt extends ConstructionMethod("1")
  case object ManufacturedHome extends ConstructionMethod("2")

  def validateConstructionMethods(rawConstructionMethods: Seq[String])
    : Either[Seq[String], Seq[ConstructionMethod]] = {
    val potentialConstructionMethods = rawConstructionMethods.map(
      constructionMethod =>
        (constructionMethod,
         ConstructionMethod.withNameInsensitiveOption(constructionMethod)))

    val isValidConstructionMethod =
      potentialConstructionMethods.map(_._2).forall(_.isDefined)

    if (isValidConstructionMethod)
      Right(potentialConstructionMethods.flatMap(_._2))
    else
      Left(potentialConstructionMethods.collect {
        case (input, None) => input
      })
  }
}
