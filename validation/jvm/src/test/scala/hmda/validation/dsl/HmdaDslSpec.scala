package hmda.validation.dsl

import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class HmdaDslSpec extends PropSpec with PropertyChecks with MustMatchers with CommonDsl with HmdaDsl {

  property("Timestamp format is correct") {
    forAll(timestampGen) { ts =>
      whenever(ts > 0) {
        ts.toString is validTimestampFormat mustBe Success()
      }
    }
  }

  property("Timestamp format is incorrect") {
    forAll(badTimestampGen) { ts =>
      whenever(ts != "") {
        ts is validTimestampFormat mustBe Failure("invalid timestamp format")
      }
    }
  }

  //TODO: improve this by generating more random values
  implicit def timestampGen: Gen[Long] = {
    Gen.oneOf(201602021453L, 201602051234L)
  }

  implicit def badTimestampGen: Gen[String] = {
    Gen.alphaStr
  }

}
