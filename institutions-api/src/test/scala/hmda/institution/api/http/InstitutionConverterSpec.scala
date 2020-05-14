
package hmda.institution.api.http

import hmda.model.institution.Institution
import org.scalatest.{ Matchers, WordSpec }
import hmda.model.institution.InstitutionGenerators._

class InstitutionConverterSpec extends WordSpec with Matchers {
  "convert institution to institution entity" in {
    lazy val sampleInstitution: Institution = institutionGen.sample.getOrElse(sampleInstitution)
    InstitutionConverter.convert(sampleInstitution).activityYear shouldBe sampleInstitution.activityYear
  }

  "empty strings in entity are None" in {
    lazy val sampleInstitution: Institution = institutionGen.sample.getOrElse(sampleInstitution)
    val entity = InstitutionConverter
      .convert(sampleInstitution)
      .copy(id2017 = "", taxId = "", respondentName = "", respondentState = "", respondentCity = "", parentName = "", topHolderName = "")

    entity.isEmpty shouldBe false

    val institution = InstitutionConverter.convert(entity, Nil)
    institution.taxId shouldBe None
  }
}