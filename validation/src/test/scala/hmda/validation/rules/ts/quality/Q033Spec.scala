package hmda.validation.rules.ts.quality

import hmda.model.fi.ts.{ Parent, TransmittalSheet }
import hmda.model.institution.ExternalIdType.FederalTaxId
import hmda.model.institution._
import hmda.model.institution.InstitutionType._
import hmda.validation.context.ValidationContext
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import org.scalacheck.Gen

class Q033Spec extends TsEditCheckSpec {

  private var institution: Institution = _
  override def check: EditCheck[TransmittalSheet] = Q033.inContext(ValidationContext(Some(institution), None))

  private val applicableTypes = Set(Bank, SavingsAndLoan, IndependentMortgageCompany)
  private val otherTypes = InstitutionType.values.toSet -- applicableTypes

  private val boolGen: Gen[Boolean] = Gen.oneOf(true, false)

  property("any TS must pass for Institution of type other than bank, savings assn, or independent mortgage company") {
    forAll(tsGen, Gen.oneOf(otherTypes.toList), Gen.alphaStr) { (ts, otherType, eitherWay) =>
      whenInstitution(instType = otherType, eitherWay)
      ts.mustPass
    }
  }

  property("TS must pass if parent info is present and respondent is a bank, savings assn, or indep mortgage company") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList), Gen.alphaStr) { (ts, relevantType, eitherWay) =>
      whenInstitution(instType = relevantType, eitherWay)
      val validTS = ts.copy(parent = Parent("a bank", "12 Main St", "Washington", "DC", "12345"))
      validTS.mustPass
    }
  }

  property("TS must fail if parent info is missing, when respondent is of a relevant type and has a parent company") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, relevantType) =>
      whenInstitution(instType = relevantType, parentName = "some parent")
      val invalidTS = ts.copy(parent = Parent("", "", "", "DC", "12345"))
      invalidTS.mustFail
    }
  }

  property("TS must pass if parent info is missing, when respondent is of a relevant type but has NO parent company") {
    forAll(tsGen, Gen.oneOf(applicableTypes.toList)) { (ts, relevantType) =>
      whenInstitution(instType = relevantType, parentName = "")
      val validTS = ts.copy(parent = Parent("", "", "", "", ""))
      validTS.mustPass
    }
  }

  private def whenInstitution(instType: InstitutionType, parentName: String): Unit = {
    institution = Institution("22", Agency.CFPB, 2017, instType, cra = true, Set(), EmailDomains("", "", ""),
      Respondent(ExternalId("", FederalTaxId), "some bank", "", "", ""), hmdaFilerFlag = true,
      hmda.model.institution.Parent("", 0, parentName, "", ""), 0, 0, TopHolder(0, "", "", "", ""))
  }
}
