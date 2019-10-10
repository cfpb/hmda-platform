package hmda.api.http.codec.filing.submission

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import ParsingErrorSummaryGenerator._
import hmda.api.http.model.filing.submissions.ParsingErrorSummary
import io.circe.syntax._

class ParsingErrorSummaryCodecSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("ParsingErrorSummary must encode/decode to/from JSON") {
    forAll(parsingErrorSummaryGen) { parsingErrorSummary =>
      whenever(!parsingErrorSummary.isEmpty) {
        val json = parsingErrorSummary.asJson
        val encoded = json
          .as[ParsingErrorSummary]
          .getOrElse(ParsingErrorSummary())

        encoded.transmittalSheetErrors mustBe parsingErrorSummary.transmittalSheetErrors
        encoded.larErrors mustBe parsingErrorSummary.larErrors
        encoded.path mustBe parsingErrorSummary.path
        encoded.total mustBe parsingErrorSummary.total
        encoded.status mustBe parsingErrorSummary.status
      }
    }
  }
}
