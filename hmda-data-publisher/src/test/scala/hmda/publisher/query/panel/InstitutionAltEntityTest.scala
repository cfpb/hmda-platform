package hmda.publisher.query.panel

import com.softwaremill.diffx.Diff
import hmda.utils.DiffxMatcher
import org.scalatest.{ MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.ScalacheckShapeless._

class InstitutionAltEntityTest extends PropSpec with ScalaCheckPropertyChecks with MustMatchers with DiffxMatcher {

  implicit val diffForBool: Diff[Boolean] = Diff.useEquals[Boolean] // missing in the lib?
  implicit val institutionAltEntityDiff = Diff.derived[InstitutionAltEntity]

  property("InstitutionAltEntity must convert to and from psv") {
    forAll { (source: InstitutionAltEntity) =>
      val psvRow  = source.toPSV
      val larFromPsv = InstitutionAltEntity.parseFromPSVUnsafe(psvRow)
      larFromPsv must matchTo(source)
    }
  }

}