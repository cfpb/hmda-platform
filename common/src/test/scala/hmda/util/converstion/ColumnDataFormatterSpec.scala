package hmda.util.conversion

import org.scalacheck.Gen
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ColumnDataFormatterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  val formatter: ColumnDataFormatter = new ColumnDataFormatter {}

  property("control characters must be properly removed") {
    forAll(Gen.alphaStr) { v =>
      formatter.controlCharacterFilter(v) mustBe v.filter(_ >= ' ')
    }
  }
}