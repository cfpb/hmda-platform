package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LarGenerators
import hmda.model.institution.InstitutionType.{ Bank, IndependentMortgageCompany }
import hmda.model.institution.{ Agency, Institution }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Success
import hmda.validation.dsl.Failure
import hmda.validation.rules.lar.BadValueUtils
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class Q595Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators with BadValueUtils {

  val baseInstitution = Institution.empty.copy(id = "6999998")
  val nonDepository = baseInstitution.copy(institutionType = IndependentMortgageCompany)
  val depository = baseInstitution.copy(institutionType = Bank)

  val actionGen = Gen.oneOf(1, 2, 3, 4, 5, 7, 8)

  property("Passes if no institution is provided") {
    forAll(larGen) { lar =>
      val ctx = ValidationContext(None, None)
      Q595.inContext(ctx)(lar) mustBe Success()
    }
  }

  property("Passes with a non-applicable action taken type") {
    forAll(larGen) { lar =>
      val newLar = lar.copy(actionTakenType = 6)
      val ctx = ValidationContext(Some(depository), None)

      Q595.inContext(ctx)(newLar) mustBe Success()
    }
  }

  property("Passes with a non-applicable institution type") {
    forAll(larGen, actionGen) { (lar, action) =>
      val newLar = lar.copy(actionTakenType = action)
      val ctx = ValidationContext(Some(nonDepository), None)

      Q595.inContext(ctx)(newLar) mustBe Success()
    }
  }

  property("Passes if MSA is NA") {
    forAll(larGen, actionGen) { (lar, action) =>
      val newGeo = lar.geography.copy(msa = "NA")
      val newLar = lar.copy(geography = newGeo, actionTakenType = action)

      val ctx = ValidationContext(Some(depository), None)

      Q595.inContext(ctx)(newLar) mustBe Success()
    }
  }

  property("Passes if MSA is contained in institution's panel") {
    forAll(larGen, actionGen) { (lar, action) =>
      val newGeo = lar.geography.copy(msa = "45460")
      val newLar = lar.copy(geography = newGeo, actionTakenType = action)

      val ctx = ValidationContext(Some(depository), None)

      Q595.inContext(ctx)(newLar) mustBe Success()
    }
  }

  property("Fails if MSA is not contained in institution's panel") {
    forAll(larGen, actionGen) { (lar, action) =>
      val newGeo = lar.geography.copy(msa = "some other number")
      val newLar = lar.copy(geography = newGeo, actionTakenType = action)

      val ctx = ValidationContext(Some(depository), None)

      Q595.inContext(ctx)(newLar) mustBe Failure()
    }
  }
}
