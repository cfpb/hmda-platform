package hmda.validation.engine

import hmda.model.filing.ts.TsGenerators._
import hmda.model.institution.{ CFPB, FDIC, Institution }
import hmda.model.validation.{ SyntacticalValidationError, TsValidationError, ValidityValidationError }
import hmda.utils.YearUtils.Period
import hmda.validation.context.ValidationContext
import hmda.validation.engine.TsEngine2021._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TsEngine2027Spec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {
  private val tsGen2027Yearly = tsGen.map(_.copy(year = 2027))

  property("Ts Validation Engine must pass all checks") {
    forAll(tsGen2027Yearly) { ts =>
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
    forAll(tsGen2027Yearly) { ts =>
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

  property("Ts Validation Engine must capture S303 (wrong lei, agency, or tax id)") {
    forAll(tsGen2027Yearly) { ts =>
      whenever(
        ts.LEI != "" &&
          ts.agency.code != 0 &&
          ts.taxId != ""
      ) {
        val institution = Institution.empty.copy(
          LEI = "TEST1234567890123456",
          agency = CFPB,
          taxId = Option("12-3456789")
        )
        val testContext = ValidationContext(Option(institution), Some(Period(ts.year, None)))
        val validation = checkAll(ts.copy(LEI = "TEST1234567890123457", agency = FDIC, taxId = "23-4567890"), institution.LEI, testContext, TsValidationError)
        val errors = validation.leftMap(errors => errors.toList).toEither.left.get
        errors must contain(SyntacticalValidationError(institution.LEI, "S303", TsValidationError))
      }
    }
  }

}