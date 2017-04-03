package hmda.api.protocol.admin

import hmda.api.model.ModelGenerators
import hmda.model.institution.InstitutionGenerators._
import hmda.model.institution.Institution
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json.{ JsArray, JsNumber, JsString, _ }

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
          ("agency", JsString(i.agency.name)),
          ("activityYear", JsNumber(i.activityYear)),
          ("institutionType", JsString(i.institutionType.entryName)),
          ("cra", JsBoolean(i.cra)),
          ("externalIds", JsArray(i.externalIds.map { x =>
            JsObject(
              ("value", JsString(x.value)),
              ("externalIdType", JsObject(
                ("code", JsString(x.externalIdType.entryName)),
                ("name", JsString(x.externalIdType.formattedName))
              ))
            )
          }.toVector)),
          ("emailDomains", JsArray(i.emailDomains.map { e =>
            JsString(e)
          }.toVector)),
          ("respondent", JsObject(
            ("externalId", JsObject(
              ("value", JsString(i.respondent.externalId.value)),
              ("externalIdType", JsObject(
                ("code", JsString(i.respondent.externalId.externalIdType.entryName)),
                ("name", JsString(i.respondent.externalId.externalIdType.formattedName))
              ))
            )),
            ("name", JsString(i.respondent.name)),
            ("state", JsString(i.respondent.state)),
            ("city", JsString(i.respondent.city)),
            ("fipsStateNumber", JsString(i.respondent.fipsStateNumber))
          )),
          ("hmdaFilerFlag", JsBoolean(i.hmdaFilerFlag)),
          ("parent", JsObject(
            ("respondentId", JsString(i.parent.respondentId)),
            ("idRssd", JsNumber(i.parent.idRssd)),
            ("name", JsString(i.parent.name)),
            ("city", JsString(i.parent.city)),
            ("state", JsString(i.parent.state))
          )),
          ("assets", JsNumber(i.assets)),
          ("otherLenderCode", JsNumber(i.otherLenderCode)),
          ("topHolder", JsObject(
            ("idRssd", JsNumber(i.topHolder.idRssd)),
            ("name", JsString(i.topHolder.name)),
            ("city", JsString(i.topHolder.city)),
            ("state", JsString(i.topHolder.state)),
            ("country", JsString(i.topHolder.country))
          ))
        )
    }
  }

}
