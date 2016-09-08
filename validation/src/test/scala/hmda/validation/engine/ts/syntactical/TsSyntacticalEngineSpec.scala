package hmda.validation.engine.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.context.ValidationContext
import org.scalacheck.Gen
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.time.{ Millis, Seconds, Span }

import scala.concurrent.ExecutionContext
import scalaz.{ Failure, Success }

class TsSyntacticalEngineSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators with TsSyntacticalEngine with ScalaFutures {

  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(2, Millis))

  implicit def badIdGen: Gen[Int] = {
    Gen.choose(2, 10)
  }

  implicit def badAgencyCodeGen: Gen[Int] = {
    Gen.oneOf(0, 4, 6, 10)
  }

  implicit def badTimestamp: Gen[Int] = {
    Gen.oneOf(0, 1000)
  }

  property("Transmittal Sheet must be valid") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val testTs = ts.copy(activityYear = 2017)
        passGenTs(testTs)
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

  property("Transmittal Sheet fails S028 (Timestamp Format)") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val badTs = for {
          t <- badTimestamp
          x = ts.copy(timestamp = t)
        } yield x

        failGenTs(badTs)
      }
    }
  }

  protected def failGenTs(badTs: Gen[TransmittalSheet]): Assertion = {
    badTs.sample match {
      case Some(x) =>
        checkSyntactical(x, ValidationContext(None)) mustBe a[Failure]
      case None => throw new scala.Exception("Test failed")
    }
  }

  protected def passGenTs(goodTs: Gen[TransmittalSheet]): Assertion = {
    goodTs.sample match {
      case Some(x) =>
        checkSyntactical(x, ValidationContext(None)) mustBe a[Success]
      case None => throw new scala.Exception("Test failed")
    }
  }

}
