package hmda.api.http.codec.filing

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.filing.FilingGenerator._
import hmda.model.filing.{FilingStatus, NotStarted}
import io.circe.syntax._
import FilingStatusCodec._

class FilingStatusCodecSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Filing Status must encode/decode to/from JSON") {
    forAll(filingStatusGen) { status =>
      val json = status.asJson
      json.as[FilingStatus].getOrElse(NotStarted) mustBe status
    }
  }

}
