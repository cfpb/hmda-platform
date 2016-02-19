package hmda.validation.rules.syntactical.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class S100Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  //Generate all Transmittal Sheets with activityYear = 2016
  override implicit def tsGen: Gen[TransmittalSheet] = {
    for {
      code <- agencyCodeGen
      timeStamp <- timeGen
      activityYear = 2016
      taxId <- taxIdGen
      totalLines = 10000
      respondent <- respondentGen
      parent <- parentGen
      contact <- contactGen
    } yield TransmittalSheet(
      1,
      code,
      timeStamp,
      activityYear,
      taxId,
      totalLines,
      respondent,
      parent,
      contact
    )
  }

  property("Activity year must be the year being processed") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S100(ts, 2016) mustBe Success()
      }
    }
  }

  property("Transmittal Sheet has invalid activity year") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S100(ts, 2017) mustBe Failure("not equal to 2017")
      }
    }
  }
}

