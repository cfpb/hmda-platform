package hmda.util
import io.chrisdavenport.cormorant.Read
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.implicits._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class PsvParsingCompanionSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {
  case class TestEntity(v1: Int, v2: String, v3: String)

  val parsingCompanion: PsvParsingCompanion[TestEntity] = new PsvParsingCompanion[TestEntity] {
    override val psvReader: Read[PsvParsingCompanionSpec.this.TestEntity] = deriveRead
  }

  property("able to parse lines with quotes without throwing exception") {
    val v1 = 1
    val v2 = "some \"quoted\" value"
    val v3 = "some other unquoted value"
    val inputPsvRow = s"$v1|$v2|$v3"
    val entity = parsingCompanion.parseFromPSVUnsafe(inputPsvRow)
    entity.v1 mustBe v1
    entity.v2 mustBe v2
    entity.v3 mustBe v3
  }

}
