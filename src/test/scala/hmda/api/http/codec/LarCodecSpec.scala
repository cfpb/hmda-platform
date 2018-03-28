package hmda.api.http.codec

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.filing.lar.LarGenerators._
import LarCodec._
import hmda.model.filing.lar.{LarIdentifier, Loan}
import io.circe.syntax._

class LarCodecSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Lar Identifier must encode/decode to/from JSON") {
    forAll(larIdentifierGen) { larId =>
      val json = larId.asJson
      json.as[LarIdentifier].getOrElse(LarIdentifier()) mustBe larId
    }
  }

  property("LOan must encode/decode to/from JSON") {
    forAll(loanGen) { loan =>
      val json = loan.asJson
      json.as[Loan].getOrElse(Loan()) mustBe loan
    }
  }

}
