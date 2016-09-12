package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.{ Parent, TransmittalSheet }
import hmda.model.institution.InstitutionType._
import hmda.model.institution.{ Agency, Institution, InstitutionType }
import hmda.validation.context.ValidationContext
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import org.scalacheck.Gen

class V110Spec extends TsEditCheckSpec {

  private var institution: Institution = _

  override def check: EditCheck[TransmittalSheet] = V110.inContext(ValidationContext(Some(institution), None))

  private val applicableTypes: Set[InstitutionType] = Set(MBS, Affiliate)

  property("any TS must pass for respondent Institution of type other than MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(InstitutionType.values)) { (ts, instType) =>
      whenever(!applicableTypes.contains(instType)) {
        whenInstitutionTypeIs(instType)
        ts.mustPass
      }
    }
  }

  property("TS must pass if parent info is populated and respondent is MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val validTS = ts.copy(parent = Parent("a bank", "12 Main St", "Washington", "DC", "12345"))
      validTS.mustPass
    }
  }

  property("TS must fail if parent name is missing and respondent is MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(name = ""))
      invalidTS.mustFail
    }
  }

  property("TS must fail if parent address is missing and respondent is MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(address = ""))
      invalidTS.mustFail
    }
  }

  property("TS must fail if parent city is missing and respondent is MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(city = ""))
      invalidTS.mustFail
    }
  }

  property("TS must fail if parent state is missing and respondent is MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(state = ""))
      invalidTS.mustFail
    }
  }

  property("TS must fail if parent zip is missing and respondent is MBS or Affiliate") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val invalidTS = ts.copy(parent = ts.parent.copy(zipCode = ""))
      invalidTS.mustFail
    }
  }

  private def whenInstitutionTypeIs(instType: InstitutionType): Unit = {
    // note: the hasParent boolean is not used in this edit. it's false here, which is not realistic for all types.
    institution = Institution("22", "some bank", Set(), Agency.CFPB, instType, hasParent = false)
  }
}
