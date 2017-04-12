package hmda.validation.rules.lar.`macro`

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LarGenerators
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import scala.concurrent.duration._
import hmda.validation.ValidationStats._

class Q011Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {
  val configuration = ConfigFactory.load()
  val larSize = configuration.getInt("hmda.validation.macro.Q011.numOfTotalLars")
  val multiplier = configuration.getDouble("hmda.validation.macro.Q011.numOfLarsMultiplier")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit override def executionContext = system.dispatcher

  val validationStats = createValidationStats(system)

  implicit val timeout = 5.seconds

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "Q011" must {
    val currentYear = 2017
    val lastYear = currentYear - 1

    "be named Q011 when institution and year are present" in {
      val ctx = ValidationContext(Some(Institution.empty), Some(currentYear))
      Q011.inContext(ctx).name mustBe "Q011"
    }

    "be named empty when institution is not present" in {
      val ctx = ValidationContext(None, Some(currentYear))
      Q011.inContext(ctx).name mustBe "empty"
    }

    "be named empty when year is not present" in {
      val ctx = ValidationContext(Some(Institution.empty), None)
      Q011.inContext(ctx).name mustBe "empty"
    }

    "succeed when previous and current lar count are less than configured value" in {
      val instId = "instId1"
      val ctx = generateValidationContext(currentYear, instId)
      val currentYearCount = Gen.choose(1, larSize - 1).sample.getOrElse(0)
      val lastYearCount = Gen.choose(1, larSize - 1).sample.getOrElse(0)
      val larSource1 = generateLarSource(currentYearCount)
      sendValidationStats(validationStats, instId, 1, lastYear, lastYearCount)
      Q011.inContext(ctx)(larSource1).map(r => r mustBe a[Success])
    }

    "succeed when previous count is greater than configured value and current count is within range" in {
      val instId = "instId2"
      val ctx = generateValidationContext(currentYear, instId)
      val lastYearCount = Gen.choose(larSize, larSize * 2).sample.getOrElse(0)
      val lower = (1 - multiplier) * lastYearCount
      val upper = (1 + multiplier) * lastYearCount
      val currentYearCount = Gen.choose(lower.toInt, upper.toInt).sample.getOrElse(0)
      val larSource = generateLarSource(currentYearCount)
      sendValidationStats(validationStats, instId, 1, lastYear, lastYearCount)
      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Success])
    }
    "fail when previous count is greater than configured value and current count is out of range" in {
      val instId = "instId3"
      val ctx = generateValidationContext(currentYear, instId)
      val lastYearCount = Gen.choose(larSize, larSize * 2).sample.getOrElse(0)
      val currentYearCount = larSize / 2
      val larSource = generateLarSource(currentYearCount)
      sendValidationStats(validationStats, instId, 1, lastYear, lastYearCount)
      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Failure])
    }

    "succeed when current count is greater than configured value, and comparison is within range" in {
      val instId = "respId4"
      val ctx = generateValidationContext(currentYear, instId)
      val currentYearCount = Gen.choose(larSize + 1, larSize * 2).sample.getOrElse(0)
      val lower = (1 - multiplier) * currentYearCount
      val upper = (1 + multiplier) * currentYearCount
      val lastYearCount = Gen.choose(lower.toInt, upper.toInt).sample.getOrElse(0)
      val larSource = generateLarSource(currentYearCount.toInt)
      sendValidationStats(validationStats, instId, 1, lastYear, lastYearCount)
      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Success])
    }

    "fail when current count is greater than configured value, and comparison is out of range" in {
      val instId = "respId5"
      val ctx = generateValidationContext(currentYear, instId)
      val currentYearCount = Gen.choose(larSize, larSize * 2).sample.getOrElse(0)
      val lastYearCount = larSize / 2
      val larSource = generateLarSource(currentYearCount)
      sendValidationStats(validationStats, instId, 1, lastYear, lastYearCount)
      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Failure])
    }

  }

  private def generateValidationContext(currentYear: Int, institutionId: String): ValidationContext = {
    ValidationContext(Some(Institution.empty.copy(id = institutionId)), Some(currentYear))
  }

  private def generateLarSource(nCurrentLars: Int) = {
    val lars = larNGen(nCurrentLars).sample.getOrElse(List())
    val larSource = Source.fromIterator(() => lars.toIterator)
    larSource
  }

  private def sendValidationStats(validationStats: ActorRef, institutionId: String, seqNr: Int, lastYear: Int, lastYearCount: Int): Unit = {
    validationStats ! AddSubmissionStats(SubmissionStats(SubmissionId(institutionId, lastYear.toString, 1), lastYearCount))
  }
}
