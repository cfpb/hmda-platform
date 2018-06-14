package hmda.api.http.codec.institution

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.InstitutionGenerators._
import io.circe.syntax._
import hmda.model.institution.Respondent
import hmda.api.http.codec.institution.RespondentCodec._

class RespondentCodecSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  property("Respondent must encode/decode to/from JSON") {
    forAll(institutionRespondentGen) { respondent =>
      val json = respondent.asJson
      json.as[Respondent].getOrElse(Respondent.empty) mustBe respondent
    }
  }

}
