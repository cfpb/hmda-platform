package hmda.api.http.codec.filing.submission

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import ParsingErrorSummaryGenerator._
import ParsingErrorSummaryCodec._
import hmda.api.http.model.filing.submissions.ParsingErrorSummary
import io.circe.syntax._

class ParsingErrorSummaryCodecSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("ParsingErrorSummary must encode/decode to/from JSON") {
    forAll(parsingErrorSummaryGen) { parsingErrorSummary =>
      val json = parsingErrorSummary.asJson
      val encoded = json
        .as[ParsingErrorSummary]
        .getOrElse(ParsingErrorSummary())

      if (encoded.isEmpty) encoded mustBe ParsingErrorSummary()
      else encoded mustBe parsingErrorSummary

    }
  }
}
