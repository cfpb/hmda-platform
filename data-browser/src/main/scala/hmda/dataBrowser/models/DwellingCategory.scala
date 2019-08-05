package hmda.dataBrowser.models

import enumeratum._
import scala.collection.immutable

sealed abstract class DwellingCategory(override val entryName: String)
    extends EnumEntry

object DwellingCategory extends Enum[DwellingCategory] {
  val values: immutable.IndexedSeq[DwellingCategory] = findValues

  case object SingleFamilySiteBuilt
      extends DwellingCategory("Single Family (1-4 Units):Site-Built")
  case object MultiFamilySiteBuilt
      extends DwellingCategory("Multifamily:Site-Built")
  case object SingleFamilyManufactured
      extends DwellingCategory("Single Family (1-4 Units):Manufactured")
  case object MultiFamilyManufactured
      extends DwellingCategory("Multifamily:Manufactured")

  def validateDwellingCategories(rawDwellingCategories: Seq[String])
    : Either[Seq[String], Seq[DwellingCategory]] = {
    val potentialDwellingCategories = rawDwellingCategories.map(
      dwellingCategory =>
        (dwellingCategory,
         DwellingCategory.withNameInsensitiveOption(dwellingCategory)))

    val isValidDwellingCategory =
      potentialDwellingCategories.map(_._2).forall(_.isDefined)

    if (isValidDwellingCategory)
      Right(potentialDwellingCategories.flatMap(_._2))
    else
      Left(potentialDwellingCategories.collect {
        case (input, None) => input
      })
  }
}
