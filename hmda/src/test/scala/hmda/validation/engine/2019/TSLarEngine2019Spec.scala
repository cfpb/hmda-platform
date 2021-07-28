package hmda.validation.engine

import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.ts.{ TransmittalLar, TransmittalSheet }
import hmda.model.validation.TsValidationError
import hmda.utils.YearUtils.Period
import hmda.validation.context.ValidationContext
import hmda.validation.engine.TsLarEngine2019._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{ Millis, Minutes, Span }
import org.scalatest.{ MustMatchers, WordSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class TsLarEngine2019Spec extends WordSpec with ScalaCheckPropertyChecks with MustMatchers with Eventually {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  "Transmittal Lar Validation Engine for 2019" must {
    "pass all checks for a valid entry" in {
      eventually {
        lazy val ts: TransmittalSheet = tsGen
          .filter(ts =>
            ts.contact.name != "" &&
              ts.contact.email != "" &&
              ts.contact.address.street != "" &&
              ts.contact.address.city != "" &&
              ts.institutionName != ""
          )
          .sample
          .getOrElse(ts)
        val tsLar: TransmittalLar = TransmittalLar(ts.copy(totalLines = 10), "example-ULI", 10, 10, 10,10, 1, Map.empty, Map.empty)
        val testContext           = ValidationContext(None, Some(Period(2019, None)))
        val validation            = checkAll(tsLar, ts.id.toString, testContext, TsValidationError)
        validation.leftMap(errors => errors.toList mustBe empty)
      }
    }

    "capture errors" in {
      forAll(tsGen) { ts =>
        eventually {
          val tsLar: TransmittalLar = TransmittalLar(ts, "example-ULI", 10, 10 ,10,9, 1, Map("example-ULI" -> List(1)), Map("example-ULI" -> List(1)))
          val testContext           = ValidationContext(None, Some(Period(2019, None)))
          val validation            = checkAll(tsLar, ts.id.toString, testContext, TsValidationError)
          validation.leftMap(errors => errors.toList must not be empty)
        }
      }
    }
  }
}