package hmda.validation.engine

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import hmda.model.filing.ts.TsGenerators._
import TsEngine._
import hmda.model.validation.{
  SyntacticalValidationError,
  ValidityValidationError
}

class TsEngineSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Ts Validation Engine must pass all checks") {
    forAll(tsGen) { ts =>
      whenever(
        ts.contact.name != "" &&
          ts.contact.email != "" &&
          ts.contact.address.street != "" &&
          ts.contact.address.city != "" &&
          ts.institutionName != "") {
        val validation = checkAll(ts, ts.LEI)
        validation.leftMap(errors => errors.toList.size mustBe 0)
      }
    }
  }

  property(
    "Ts Validation Engine must capture S300 (wrong id) and V602 (wrong quarter)") {
    forAll(tsGen) { ts =>
      whenever(
        ts.contact.name != "" &&
          ts.contact.email != "" &&
          ts.contact.address.street != "" &&
          ts.contact.address.city != "" &&
          ts.institutionName != "") {
        val validation = checkAll(ts.copy(id = 2, quarter = 2), ts.LEI)
        val errors =
          validation.leftMap(errors => errors.toList).toEither.left.get
        errors mustBe
          List(SyntacticalValidationError(ts.LEI, "S300"),
               ValidityValidationError(ts.LEI, "V602"))
      }
    }
  }

}
