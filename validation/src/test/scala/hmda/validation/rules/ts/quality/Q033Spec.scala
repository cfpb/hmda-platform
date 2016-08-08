package hmda.validation.rules.ts.quality

import hmda.model.fi.ts.{ Parent, TransmittalSheet }
import hmda.model.institution.{ Agency, Institution, InstitutionType }
import hmda.model.institution.InstitutionType._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import org.scalacheck.Gen

class Q033Spec extends TsEditCheckSpec {

  private var institution: Institution = _
  override def check: EditCheck[TransmittalSheet] = new Q033(institution)

  private val applicableTypes: Set[InstitutionType] = Set(Bank, SavingsAndLoan, IndependentMortgageCompany)

  property("any TS must pass for Institution of type other than bank, savings assn, or independent mortgage company") {
    forAll(tsGen, Gen.oneOf(InstitutionType.values)) { (ts, instType) =>
      whenever(!applicableTypes.contains(instType)) {
        whenInstitutionTypeIs(instType)
        ts.mustPass
      }
    }
  }

  property("TS must pass if parent info is present and filer is a bank, savings assn, or indep mortgage company") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, instType) =>
      whenInstitutionTypeIs(instType)
      val validTS = ts.copy(parent = Parent("a bank", "12 Main St", "Washington", "DC", "12345"))
      validTS.mustPass
    }
  }

  property("TS must fail if parent name is missing, filer is of an applicable type, and filer has a parent company") {
    // TODO implement
  }

  // TODO try switching to a test style that would make common setup/branching cases more obvious

  private def whenInstitutionTypeIs(instType: InstitutionType): Unit = {
    institution = Institution(22, "some bank", Set(), Agency.CFPB, instType)
  }
}
