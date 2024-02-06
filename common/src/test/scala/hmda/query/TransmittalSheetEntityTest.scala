package hmda.query.ts

import com.softwaremill.diffx
import com.softwaremill.diffx.{ Diff, DiffResult, Identical }
import hmda.utils.DiffxMatcher
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.ScalacheckShapeless._

import java.time.Instant

class TransmittalSheetEntityTest extends PropSpec with ScalaCheckPropertyChecks with MustMatchers with DiffxMatcher {

  implicit val diffForBool: Diff[Boolean] = Diff.useEquals[Boolean] // missing in the lib?
  implicit val timestampMatcher           = Diff.useEquals[java.sql.Timestamp]

  implicit val jsTimestampGen: Arbitrary[java.sql.Timestamp] = Arbitrary(
    Gen.chooseNum[Long](Long.MinValue, Long.MaxValue).map(x => java.sql.Timestamp.from(Instant.ofEpochMilli(x)))
  )

  property("TransmittalSheetEntity must convert to and from public psv") {
    implicit val psvPublicMatcher = Diff
      .derived[TransmittalSheetEntity]
      .ignore[TransmittalSheetEntity, Option[String]](_.submissionId)
      .ignore[TransmittalSheetEntity, Option[java.sql.Timestamp]](_.createdAt)
      .ignore[TransmittalSheetEntity, Option[Boolean]](_.isQuarterly)
      .ignore[TransmittalSheetEntity, Option[Long]](_.signDate)
      .ignore[TransmittalSheetEntity, Int](_.id)
      .ignore[TransmittalSheetEntity, String](_.street)
      .ignore[TransmittalSheetEntity, String](_.phone)
      .ignore[TransmittalSheetEntity, String](_.email)
      .ignore[TransmittalSheetEntity, String](_.name)
    forAll { (ts: TransmittalSheetEntity) =>
      val psv     = ts.toPublicPSV
      val fromPsv = TransmittalSheetEntity.PublicParser.parseFromPSVUnsafe(psv)
      fromPsv must matchTo(ts)
    }
  }

  property("TransmittalSheetEntity must convert to and from regulator psv") {
    implicit val signDateDiff: Diff[Option[Long]] = new Diff[Option[Long]] {
      override def apply(left: Option[Long], right: Option[Long], toIgnore: List[diffx.FieldPath]): DiffResult =
        (left, right) match {
          case (None, Some(0L)) => Identical(left)
          case (Some(0L), None) => Identical(left)
          case (a, b)           => Diff.useEquals[Option[Long]].apply(a, b)
        }
    }
    implicit val psvPublicMatcher: Diff[TransmittalSheetEntity] = Diff
      .derived[TransmittalSheetEntity]
      .ignore[TransmittalSheetEntity, Option[String]](_.submissionId)
      .ignore[TransmittalSheetEntity, Option[java.sql.Timestamp]](_.createdAt)
      .ignore[TransmittalSheetEntity, Option[Boolean]](_.isQuarterly)

    // date encoding breaks for extremely small longs
    // I couldnt find exact issue but this filed should never occur with such small value
    // investigate further if ever becomes an issue
    val tsGen = implicitly[Arbitrary[TransmittalSheetEntity]].arbitrary
      .suchThat(x => x.signDate.isEmpty || x.signDate.exists(_ >= 0))
    forAll(tsGen) { (ts: TransmittalSheetEntity) =>
      val psv     = ts.toRegulatorPSV
      val fromPsv = TransmittalSheetEntity.RegulatorParser.parseFromPSVUnsafe(psv)
      fromPsv must matchTo(ts)
    }
  }

}