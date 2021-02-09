package hmda.publisher.query.lar

import com.softwaremill.diffx
import com.softwaremill.diffx.{Diff, DiffResult}
import hmda.utils.DiffxMatcher
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ModifiedLarEntityImplTest extends PropSpec with ScalaCheckPropertyChecks with MustMatchers with DiffxMatcher {

  implicit val optStrDiff: Diff[Option[String]] = new Diff[Option[String]] {
    override def apply(left: Option[String], right: Option[String], toIgnore: List[diffx.FieldPath]): DiffResult =
      (left, right) match {
        case (None, Some("")) => diffx.Identical(left)
        case (Some(""), None) => diffx.Identical(left)
        case (x, y)           => Diff.useEquals[Option[String]].apply(x, y)
      }
  }

  implicit val modifiedLarPartOneDiff   = Diff.derived[ModifiedLarPartOne].ignore[ModifiedLarPartOne, Option[String]](_.loanFlag)
  implicit val modifiedLarPartTwoDiff   = Diff.derived[ModifiedLarPartTwo]
  implicit val modifiedLarPartThreeDiff = Diff.derived[ModifiedLarPartThree]
  implicit val modifiedLarPartFourDiff  = Diff.derived[ModifiedLarPartFour]
  implicit val modifiedLarPartFiveDiff  = Diff.derived[ModifiedLarPartFive]
  implicit val modifiedLarPartSixDiff   = Diff.derived[ModifiedLarPartSix]

  implicit val modifiedLarEntityImplDiff = Diff.derived[ModifiedLarEntityImpl]

  property("ModifiedLarPartOne must convert to and from psv") {
    forAll { (lar: ModifiedLarPartOne) =>
      val psvRow     = lar.toPublicPSV
      val larFromPsv = ModifiedLarPartOne.parseFromPSVUnsafe(psvRow)

      // er can compare case class because custom Equality[Option[String]] doesnt work then
      larFromPsv must matchTo(lar)
    }
  }

  property("ModifiedLarEntityImpl must convert to and from psv") {
    forAll { (lar: ModifiedLarEntityImpl) =>
      val psvRow     = lar.toPublicPSV
      val larFromPsv = ModifiedLarEntityImpl.parseFromPSVUnsafe(psvRow)
      larFromPsv must matchTo(lar)
    }
  }

}