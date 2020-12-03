package hmda.validation.engine

import hmda.model.filing.ts.TsGenerators._
import hmda.model.validation.{SyntacticalValidationError, TsValidationError, ValidityValidationError}
import hmda.utils.YearUtils.Period
import hmda.validation.context.ValidationContext
import hmda.validation.engine.TsEngine2021._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TsEngine2021Spec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {
  val tsGen2020Yearly = tsGen.map(_.copy(year = 2021))

  property("Ts Validation Engine must pass all checks") {
    forAll(tsGen2020Yearly) { ts =>
      whenever(
        ts.contact.name != "" &&
          ts.contact.email != "" &&
          ts.contact.address.street != "" &&
          ts.contact.address.city != "" &&
          ts.institutionName != ""
      ) {
        val testContext = ValidationContext(None, Some(Period(ts.year, None)))
        val validation  = checkAll(ts, ts.LEI, testContext, TsValidationError)
        validation.leftMap(errors => errors.toList.size mustBe 0)
      }
    }
  }

  property("Ts Validation Engine must capture S300 (wrong id) and V602 (wrong quarter)") {
    forAll(tsGen2020Yearly) { ts =>
      whenever(
        ts.contact.name != "" &&
          ts.contact.email != "" &&
          ts.contact.address.street != "" &&
          ts.contact.address.city != "" &&
          ts.institutionName != ""
      ) {
        val testContext = ValidationContext(None, Some(Period(ts.year, None)))
        val validation  = checkAll(ts.copy(id = 2, quarter = 2), ts.LEI, testContext, TsValidationError)
        val errors      = validation.leftMap(errors => errors.toList).toEither.left.get
        errors mustBe List(SyntacticalValidationError(ts.LEI, "S300", TsValidationError), ValidityValidationError(ts.LEI, "V602", TsValidationError))
      }
    }
  }

}