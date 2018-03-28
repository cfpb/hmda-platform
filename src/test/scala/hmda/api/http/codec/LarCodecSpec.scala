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

  property("Loan must encode/decode to/from JSON") {
    forAll(loanGen) { loan =>
      val json = loan.asJson
      json.as[Loan].getOrElse(Loan()) mustBe loan
    }
  }

  property("LarAction must encode/decode to/from JSON") {
    pending
  }

  property("Geography must encode/decode to/from JSON") {
    pending
  }

  property("Applicant must encode/decode to/from JSON") {
    pending
  }

  property("Denial must encode/decode to/from JSON") {
    pending
  }

  property("LoanDisclosure must encode/decode to/from JSON") {
    pending
  }

  property("Property must encode/decode to/from JSON") {
    pending
  }

  property("AutomatedUnderwritingSystem must encode/decode to/from JSON") {
    pending
  }

  property("AutomatedUnderwritingSystemResult must encode/decode to/from JSON") {
    pending
  }

  property("LoanApplicationRegister must encode/decode to/from JSON") {
    pending
  }


}
