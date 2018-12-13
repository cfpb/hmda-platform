package hmda.validation.rules.ts.syntactical

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.model.filing.ts.TsGenerators._
import hmda.validation.context.ValidationContext

class S303Spec extends TsEditCheckSpec {

  private var institution: Institution = _

  override def check: EditCheck[TransmittalSheet] =
    S303.withContext(ValidationContext(Some(institution)))

  property("Pass when LEI, Agency and Tax ID are reported correctly") {
    forAll(tsGen) { ts =>
      whenInstitution(ts.LEI, ts.agency, ts.taxId)
      ts.mustPass
    }
  }

  property("Pass when LEI is a different case") {
    forAll(tsGen) { ts =>
      whenInstitution(ts.LEI.toLowerCase, ts.agency, ts.taxId)
      ts.mustPass
      val upperCaseTs = ts.copy(LEI = ts.LEI.toUpperCase)
      upperCaseTs.mustPass
    }
  }

  property("Fail when LEI is reported incorrectly") {
    forAll(tsGen) { ts =>
      whenInstitution(ts.LEI + "x", ts.agency, ts.taxId)
      ts.mustFail
    }
  }

  property("Fail when Agency is reported incorrectly") {
    forAll(tsGen) { ts =>
      whenever(ts.agency != CFPB) {
        whenInstitution(ts.LEI, CFPB, ts.taxId)
        ts.mustFail
      }
    }
  }

  property("Fail when Tax ID is reported incorrectly") {
    forAll(tsGen) { ts =>
      whenInstitution(ts.LEI, ts.agency, ts.taxId + "x")
      ts.mustFail
    }
  }

  property("Fail when LEI, Agency and Tax ID are reported incorrectly") {
    forAll(tsGen) { ts =>
      whenever(ts.agency != CFPB) {
        whenInstitution(ts.LEI + "x", CFPB, ts.taxId + "x")
        ts.mustFail
      }
    }
  }
  private def whenInstitution(lei: String,
                              agency: Agency,
                              taxId: String): Unit = {
    institution = Institution.empty.copy(
      LEI = lei,
      agency = agency,
      taxId = Some(taxId)
    )
  }

}
