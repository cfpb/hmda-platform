package hmda.api.http.codec.institution

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.InstitutionGenerators._
import hmda.api.http.codec.institution.TopHolderCodec._
import hmda.model.institution.TopHolder
import io.circe.syntax._

class TopHolderCodecSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("TopHolder must encode/decode to/from JSON") {
    forAll(topHolderGen) { topHolder =>
      val json = topHolder.asJson
      json.as[TopHolder].getOrElse(TopHolder.empty) mustBe topHolder
    }
  }
}
