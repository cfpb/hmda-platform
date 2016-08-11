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

  private val applicableTypes = Set(Bank, SavingsAndLoan, IndependentMortgageCompany)
  private val otherTypes = InstitutionType.values.toSet -- applicableTypes

  private val boolGen: Gen[Boolean] = Gen.oneOf(true, false)

  property("any TS must pass for Institution of type other than bank, savings assn, or independent mortgage company") {
    forAll(tsGen, Gen.oneOf(otherTypes.toList), boolGen) { (ts, otherType, eitherWay) =>
      whenInstitution(instType = otherType, hasParent = eitherWay)
      ts.mustPass
    }
  }

  property("TS must pass if parent info is present and respondent is a bank, savings assn, or indep mortgage company") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList), boolGen) { (ts, relevantType, eitherWay) =>
      whenInstitution(instType = relevantType, hasParent = eitherWay)
      val validTS = ts.copy(parent = Parent("a bank", "12 Main St", "Washington", "DC", "12345"))
      validTS.mustPass
    }
  }

  property("TS must fail if parent info is missing, when respondent is of a relevant type and has a parent company") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, relevantType) =>
      whenInstitution(instType = relevantType, hasParent = true)
      val invalidTS = ts.copy(parent = Parent("", "", "", "DC", "12345"))
      invalidTS.mustFail
    }
  }

  property("TS must pass if parent info is missing, when respondent is of a relevant type but has NO parent company") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, relevantType) =>
      whenInstitution(instType = relevantType, hasParent = false)
      val validTS = ts.copy(parent = Parent("", "", "", "", ""))
      validTS.mustPass
    }
  }

  private def whenInstitution(instType: InstitutionType, hasParent: Boolean): Unit = {
    institution = Institution(22, "some bank", Set(), Agency.CFPB, instType, hasParent)
  }
}
