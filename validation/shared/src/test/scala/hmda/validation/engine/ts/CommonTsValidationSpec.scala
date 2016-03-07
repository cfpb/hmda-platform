package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.ts.TsGenerators
import org.scalacheck.Gen
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.time.{ Seconds, Span, Millis }
import scala.concurrent.{ ExecutionContext, Future }

trait CommonTsValidationSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with CommonTsValidation with ScalaFutures {

  /*
    The following methods simulate API calls to get values from remote resources
    */

  /*
    Gets latest timestamp from database (see S013)
   */
  override def findTimestamp: Future[Long] = Future(201301111330L)

  /*
    Returns year to be processed (see S100)
   */
  override def findYearProcessed: Future[Int] = Future(2017)

  /*
  Returns control number (valid respondent id / agency code combination for date processed, see S025)
  TODO: figure out what this means (???). S025 is not implemented yet
   */
  override def findControlNumber: Future[String] = Future("")

  override implicit val ec: ExecutionContext

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(2, Millis))

  implicit def badIdGen: Gen[Int] = {
    Gen.choose(2, 10)
  }

  implicit def badAgencyCodeGen: Gen[Int] = {
    Gen.oneOf(0, 4, 6, 10)
  }

  property("Transmittal Sheet must be valid") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val testTs = ts.copy(activityYear = 2017)
        passGenTs(testTs)
      }
    }
  }

  property("Transmittal Sheet fails S100 (Activity Year)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val testTs = ts.copy(activityYear = 2019)
        failGenTs(testTs)
      }
    }
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

  property("Transmittal Sheet fails S013 (Timestamp)") {
    forAll(tsGen) { ts =>
      val badTs = ts.copy(timestamp = 1)
      failGenTs(badTs)
    }
  }

  protected def failGenTs(badTs: Gen[TransmittalSheet]): Assertion = {
    badTs.sample match {
      case Some(x) => {
        val fValidated = validate(x)
        whenReady(fValidated) { validated =>
          validated.isFailure mustBe true
        }
      }
      case None => throw new scala.Exception("Test failed")
    }
  }

  protected def passGenTs(goodTs: Gen[TransmittalSheet]): Assertion = {
    goodTs.sample match {
      case Some(x) => {
        val fValidated = validate(x)
        whenReady(fValidated) { validated =>
          validated.isSuccess mustBe true
        }
      }
      case None => throw new scala.Exception("Test failed")
    }
  }

}
