package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.model.filing.ts.TsGenerators._
import hmda.validation.context.ValidationContext
import hmda.utils.YearUtils.Period

class V718Spec extends TsEditCheckSpec {

  private var period: Period = _

  override def check: EditCheck[TransmittalSheet] =
    V718.withContext(ValidationContext(None, Some(period), None))

    property("Fail with bad quarter") {
        forAll(tsGen) { ts =>
        whenQuarter("Q5")
        ts.mustFail
        }
    }

    property("Fail with incorrect quarter") {
        forAll(tsGen) { ts =>
        whenQuarter("Q1")
        ts.copy(quarter = 2).mustFail
        }
    }

    property("Pass with correct quarter") {
        forAll(tsGen) { ts =>
        whenQuarter("Q1")
        ts.copy(quarter = 1).mustPass
        }
    }

    private def whenQuarter(quarter: String): Unit = {
        period = Period(2019, Some(quarter))
    }
}