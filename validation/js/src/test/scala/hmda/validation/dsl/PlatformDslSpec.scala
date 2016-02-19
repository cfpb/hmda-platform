package hmda.validation.dsl

import org.scalatest.{ AsyncFlatSpec, MustMatchers, PropSpec }

//TODO: I couldn't get ScalaCheck to work in JS for this test
class PlatformDslSpec extends AsyncFlatSpec with MustMatchers with CommonDsl with PlatformDsl {

  "Timestamp format" should "be correct" in {
    val ts = "201602021453"
    ts is validTimestampFormat mustBe Success()
  }

  //"Timestamp format" should "be incorrect" in {
  //  val ts = "alsmwepom"
  //  ts is validTimestampFormat mustBe Failure("invalid timestamp format")
  //}

}
