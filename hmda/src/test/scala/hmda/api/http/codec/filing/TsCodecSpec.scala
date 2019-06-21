package hmda.api.http.codec.filing

import hmda.api.http.codec.filing.TsCodec._
import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.ts._2018.{Address, Contact, TransmittalSheet}
import hmda.model.filing.ts.{Address, Contact, TransmittalSheet}
import hmda.model.institution.Agency
import io.circe.syntax._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}
import hmda.model.institution.InstitutionGenerators._

class TsCodecSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Address must encode/decode to/from JSON") {
    forAll(addressGen) { address =>
      val json = address.asJson
      json.as[Address].getOrElse(Address()) mustBe address
    }
  }

  property("Contact must encode/decode to/from JSON") {
    forAll(contactGen) { contact =>
      val json = contact.asJson
      json.as[Contact].getOrElse(Contact()) mustBe contact
    }
  }

  property("Agency must encode/decode to/from JSON") {
    forAll(agencyGen) { agency =>
      val json = agency.asJson
      json.as[Agency].getOrElse(Agency.valueOf(-1)) mustBe agency
    }
  }

  property("Transmittal Sheet must encode/decode to/from JSON") {
    forAll(tsGen) { ts =>
      val json = ts.asJson
      json.as[TransmittalSheet].getOrElse(TransmittalSheet()) mustBe ts
    }
  }

}
