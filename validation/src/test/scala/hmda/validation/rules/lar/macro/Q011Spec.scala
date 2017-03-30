package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.model.institution.ExternalIdType.RssdId
import hmda.model.institution.{ ExternalId, Institution, Respondent }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers, ParallelTestExecution }

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class Q011Spec extends AsyncWordSpec with MustMatchers with LarGenerators with BeforeAndAfterAll {
  val configuration = ConfigFactory.load()
  val larSize = configuration.getInt("hmda.validation.macro.Q011.numOfTotalLars")
  val multiplier = configuration.getDouble("hmda.validation.macro.Q011.numOfLarsMultiplier")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit override def executionContext = system.dispatcher

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

    //    "succeed when previous and current lar count are less than configured value" in {
    //      val respId = "respId1"
    //      val ctx = generateValidationContext(currentYear, respId)
    //      val n1 = Gen.choose(1, larSize - 1).sample.getOrElse(0)
    //      val n2 = Gen.choose(1, larSize - 1).sample.getOrElse(0)
    //      val larSource1 = generateLarSource(n1, n2, respId, lastYear, timeout)
    //      Q011.inContext(ctx)(larSource1).map(r => r mustBe a[Success])
    //    }
    //
    //    "succeed when previous count is greater than configured value and current count is within range" in {
    //      val respId = "respId2"
    //      val ctx = generateValidationContext(currentYear, respId)
    //      val lastYearCount = Gen.choose(larSize, larSize * 2).sample.getOrElse(0)
    //      val lower = (1 - multiplier) * lastYearCount
    //      val upper = (1 + multiplier) * lastYearCount
    //      val currentYearCount = Gen.choose(lower.toInt, upper.toInt).sample.getOrElse(0)
    //      val larSource = generateLarSource(currentYearCount, lastYearCount, respId, lastYear, timeout)
    //      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Success])
    //    }
    //
    //    "fail when previous count is greater than configured value and current count is out of range" in {
    //      val respId = "respId3"
    //      val ctx = generateValidationContext(currentYear, respId)
    //      val lastYearCount = Gen.choose(larSize, larSize * 2).sample.getOrElse(0)
    //      val currentYearCount = larSize / 2
    //      val larSource = generateLarSource(currentYearCount, lastYearCount, respId, lastYear, timeout)
    //      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Failure])
    //    }
    //
    //    "succeed when current count is greater than configured value, and comparison is within range" in {
    //      val respId = "respId4"
    //      val ctx = generateValidationContext(currentYear, respId)
    //      val currentYearCount = Gen.choose(larSize, larSize * 2).sample.getOrElse(0)
    //      val lower = (1 - multiplier) * currentYearCount
    //      val upper = (1 + multiplier) * currentYearCount
    //      val lastYearCount = Gen.choose(lower.toInt, upper.toInt).sample.getOrElse(0)
    //      val larSource = generateLarSource(currentYearCount.toInt, lastYearCount.toInt, respId, lastYear, timeout)
    //      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Success])
    //    }
    //
    //    "fail current count is greater than configured value, and comparison is out of range" in {
    //      val respId = "respId5"
    //      val ctx = generateValidationContext(currentYear, respId)
    //      val currentYearCount = Gen.choose(larSize, larSize * 2).sample.getOrElse(0)
    //      val lastYearCount = larSize / 2
    //      val larSource = generateLarSource(currentYearCount, lastYearCount, respId, lastYear, timeout)
    //      Q011.inContext(ctx)(larSource).map(r => r mustBe a[Failure])
    //    }

  }

  private def generateValidationContext(currentYear: Int, respId: String): ValidationContext = {
    val resp1 = Respondent(ExternalId(respId, RssdId), "", "", "", "")
    ValidationContext(Some(Institution.empty.copy(respondent = resp1)), Some(currentYear))
  }

  private def generateLarSource(nCurrentLars: Int, nPreviousLars: Int, respId: String, lastYear: Int, timeout: Duration) = {
    val lars = larNGen(nCurrentLars).sample.getOrElse(List())
    val larSource = Source.fromIterator(() => lars.toIterator)
    //    val lastYearLars = larNGen(nPreviousLars).sample.getOrElse(List()).map(lar => lar.copy(respondentId = respId))
    //    val lastYearList = larsToLarQueryList(lastYearLars, respId, lastYear)
    //    val resultF = Future.sequence(lastYearList.map(lar => repository.insertOrUpdate(lar)))
    //    Await.result(resultF, timeout)
    larSource
  }
}
