package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.model.filing.ts.TsGenerators._
import hmda.validation.context.ValidationContext
import hmda.utils.YearUtils.Period

class V718Q5Spec extends TsEditCheckSpec {
    override def check: EditCheck[TransmittalSheet] =
            V718.withContext(ValidationContext(None, Some(Period(2019, Some("Q5"))), None))

    property("Fail with bad quarter") {
        forAll(tsGen) { ts =>
            ts.mustFail
        }
    }

}

class V718Q1Spec extends TsEditCheckSpec {
    override def check: EditCheck[TransmittalSheet] =
            V718.withContext(ValidationContext(None, Some(Period(2019, Some("Q1"))), None))
    
    property("Fail with incorrect quarter") {
        forAll(tsGen) { ts =>
            ts.copy(quarter = 2).mustFail
        }
    }

    property("Pass with correct quarter") {
        forAll(tsGen) { ts =>
            ts.copy(quarter = 1).mustPass
        }
    }

}