package hmda.api.protocol.admin

import hmda.api.model.ModelGenerators
import hmda.model.institution.InstitutionGenerators._
import hmda.model.institution.Institution
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._

class WriteInstitutionProtocolSpec extends PropSpec with PropertyChecks with MustMatchers with ModelGenerators with WriteInstitutionProtocol {

  property("Institution should convert to and from json") {
    forAll(institutionGen) { institution =>
      institution.toJson.convertTo[Institution] mustBe institution
    }
  }

  property("Institution JSON must be the correct format") {
    forAll(institutionGen) { i =>
      i.toJson mustBe
        JsObject(
          ("id", JsString(i.id)),
          ("name", JsString(i.name)),
          ("cra", JsBoolean(i.cra)),
          ("agency", JsString(i.agency.name)),
          ("externalIds", JsArray(i.externalIds.map { x =>
            JsObject(
              ("id", JsString(x.id)),
              ("idType", JsString(x.idType.entryName))
            )
          }.toVector)),
          ("status", JsObject(
            ("code", JsNumber(i.status.code)),
            ("message", JsString(i.status.message))
          )),
          ("hasParent", JsBoolean(i.hasParent)),
          ("institutionType", JsString(i.institutionType.entryName))
        )
    }
  }

}
