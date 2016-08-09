package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.{ Parent, TransmittalSheet }
import hmda.model.institution.InstitutionType._
import hmda.model.institution.{ Agency, Institution, InstitutionType }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import org.scalacheck.Gen

class V110Spec extends TsEditCheckSpec {

  private var institution: Institution = _

  override def check: EditCheck[TransmittalSheet] = new V110(institution)

  private val applicableTypes: Set[InstitutionType] = Set(DependentMortgageCompany, Affiliate)

  property("any TS must pass for Institution of type other than MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(InstitutionType.values)) { (ts, instType) =>
      whenever(!applicableTypes.contains(instType)) {
        whenInstitutionTypeIs(instType)
        ts.mustPass
      }
    }
  }

  // TODO fix generator so that the previous test will cover this case too. (then remove this one.)
  property("any TS, even with empty parent info, must pass for Institution of type other than MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(InstitutionType.values)) { (ts, instType) =>
      whenever(!applicableTypes.contains(instType)) {
        whenInstitutionTypeIs(instType)
        val noParentTS = ts.copy(parent = Parent("", "", "", "", ""))
        noParentTS.mustPass
      }
    }
  }

  property("TS must pass if parent info is populated and Institution is dependent MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val validTS = ts.copy(parent = Parent("a bank", "12 Main St", "Washington", "DC", "12345"))
      validTS.mustPass
    }
  }

  property("TS must fail if parent name is missing and Institution is dependent MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(name = ""))
      invalidTS.mustFail
    }
  }

  property("TS must fail if parent address is missing and Institution is dependent MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(address = ""))
      invalidTS.mustFail
    }
  }

  property("TS must fail if parent city is missing and Institution is dependent MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(city = ""))
      invalidTS.mustFail
    }
  }

  property("TS must fail if parent state is missing and Institution is dependent MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(state = ""))
      invalidTS.mustFail
    }
  }

  property("TS must fail if parent zip is missing and Institution is dependent MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(zipCode = ""))
      invalidTS.mustFail
    }
  }

  private def whenInstitutionTypeIs(instType: InstitutionType): Unit = {
    // note: the hasParent boolean is not used in this edit. it's false here, which may not always be realistic.
    institution = Institution(22, "some bank", Set(), Agency.CFPB, instType, hasParent = false)
  }
}
