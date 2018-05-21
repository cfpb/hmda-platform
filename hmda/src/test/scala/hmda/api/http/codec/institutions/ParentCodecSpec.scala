package hmda.api.http.codec.institutions

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.institutions.InstitutionGenerators._
import ParentCodec._
import hmda.model.institution.Parent
import io.circe.syntax._

class ParentCodecSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Parent must encode/decode to/from JSON") {
    forAll(institutionParentGen) { parent =>
      val json = parent.asJson
      json.as[Parent].getOrElse(Parent.empty) mustBe parent
    }
  }

}
