package hmda.validation.rules.ts.syntactical

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.stats.ValidationStats._
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.messages.ValidationStatsMessages.AddSubmissionLarStatsActorRef
import hmda.validation.stats.SubmissionLarStats.createSubmissionStats
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

import scala.concurrent.duration._

class S011Spec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll {
  val configuration = ConfigFactory.load()

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit override def executionContext = system.dispatcher

  val validationStats = createValidationStats(system)

  implicit val timeout = 5.seconds

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "S011" must {
    val currentYear = 2017

    "succeed when 1 or more lars were submitted" in {
      val instId = "instId1"
      val ctx = generateValidationContext(currentYear, instId)
      sendValidationStats(validationStats, instId, currentYear, 1)
      S011.inContext(ctx)(TransmittalSheet()).map(r => r mustBe a[Success])
    }

    "fail when zero lars were submitted" in {
      val instId = "instId2"
      val ctx = generateValidationContext(currentYear, instId)
      sendValidationStats(validationStats, instId, currentYear, 0)
      S011.inContext(ctx)(TransmittalSheet()).map(r => r mustBe a[Failure])
    }

    /// Handling ValidationContext correctly
    "be named S011 when institution and year are present" in {
      val ctx = ValidationContext(Some(Institution.empty), Some(currentYear))
      S011.inContext(ctx).name mustBe "S011"
    }

    "be named empty when institution is not present" in {
      val ctx = ValidationContext(None, Some(currentYear))
      S011.inContext(ctx).name mustBe "empty"
    }

    "be named empty when year is not present" in {
      val ctx = ValidationContext(Some(Institution.empty), None)
      S011.inContext(ctx).name mustBe "empty"
    }
  }

  private def generateValidationContext(currentYear: Int, institutionId: String): ValidationContext = {
    ValidationContext(Some(Institution.empty.copy(id = institutionId)), Some(currentYear))
  }
  private def sendValidationStats(validationStats: ActorRef, institutionId: String, year: Int, submittedCount: Int): Unit = {
    val submissionId = SubmissionId(institutionId, year.toString, 1)
    val larStats = createSubmissionStats(system, submissionId)
    validationStats ! AddSubmissionLarStatsActorRef(larStats, submissionId)
    validationStats ! AddSubmissionSubmittedTotal(submittedCount, submissionId)
  }
}
