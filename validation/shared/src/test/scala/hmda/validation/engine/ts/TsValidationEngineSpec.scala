package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.ts.TsGenerators
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ Assertion, MustMatchers, PropSpec }

class TsValidationEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with TsValidationEngine {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit def badIdGen: Gen[Int] = {
    Gen.choose(2, 10)
  }

  implicit def badAgencyCodeGen: Gen[Int] = {
    Gen.oneOf(0, 4, 6, 10)
  }

  property("Transmittal Sheet must be valid") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        if (ts.activityYear == 2017) {
          val validated = validate(ts)
          println(validated)
          validated.isSuccess mustBe true
        }
      }
    }
  }

  property("Transmittal Sheet fails S100 (Activity Year)") {

  }

  property("Transmittal Sheet fails S010 (Record Identifier)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = for {
          i <- badIdGen
          t = ts.copy(id = i)
        } yield t

        failGenTs(badTs)
      }
    }
  }

  property("Transmittal Sheet fails S020 (Agency Code)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = for {
          c <- badAgencyCodeGen
          t = ts.copy(agencyCode = c)
        } yield t

        failGenTs(badTs)
      }
    }
  }

  private def failGenTs(badTs: Gen[TransmittalSheet]): Assertion = {
    badTs.sample match {
      case Some(x) => validate(x).isFailure mustBe true
      case None => throw new scala.Exception("Test failed")
    }
  }

  /*
    Gets latest timestamp from database (see S013)
     */
  override def findTimestamp: Long = 201301111330L

  /*
    Returns year to be processed (see S100)
     */
  override def findYearProcessed: Int = 2017

  /*
  Returns control number (valid respondent id / agency code combination for date processed, see S025)
  TODO: figure out what this means (???). S025 is not implemented yet
   */
  override def findControlNumber: String = ""
}
