package hmda.validation.rules.ts.syntactical

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.model.institution._
import hmda.validation.context.ValidationContext
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class S302Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] =
    S302.withContext(ValidationContext())

  property("Pass when year is reported correctly") {
    forAll(tsGen) { ts =>
      whenInstitution(ts.LEI, ts.agency, ts.taxId)
      ts.mustPass
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
