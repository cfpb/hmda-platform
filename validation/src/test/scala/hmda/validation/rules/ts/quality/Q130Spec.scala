package hmda.validation.rules.ts.quality

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.ts.{ TransmittalSheet, TsGenerators }
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.stats.ValidationStats._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import scala.concurrent.duration._

class Q130Spec extends AsyncWordSpec with MustMatchers with TsGenerators with BeforeAndAfterAll {
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

  "Q130" must {
    val currentYear = 2017
    val count = 100

    "be named Q130 when institution and year are present" in {
      val ctx = ValidationContext(Some(Institution.empty), Some(currentYear))
      Q130.inContext(ctx).name mustBe "Q130"
    }

    "be named empty when institution is not present" in {
      val ctx = ValidationContext(None, Some(currentYear))
      Q130.inContext(ctx).name mustBe "empty"
    }

    "be named empty when year is not present" in {
      val ctx = ValidationContext(Some(Institution.empty), None)
      Q130.inContext(ctx).name mustBe "empty"
    }

    "succeed when total submitted lars matches transmittal sheet" in {
      val instId = "instId1"
      val ctx = generateValidationContext(currentYear, instId)
      sendValidationStats(validationStats, instId, currentYear, count)
      Q130.inContext(ctx)(tsGenWithLarCount(count)).map(r => r mustBe a[Success])
    }

    "fail when total submitted lars does not match transmittal sheet" in {
      val instId = "instId2"
      val ctx = generateValidationContext(currentYear, instId)
      sendValidationStats(validationStats, instId, currentYear, count)
      Q130.inContext(ctx)(tsGenWithLarCount(count + 1)).map(r => r mustBe a[Failure])
    }

  }

  private def tsGenWithLarCount(count: Int): TransmittalSheet = {
    tsGen.sample.getOrElse(TransmittalSheet()).copy(totalLines = count)
  }

  private def generateValidationContext(currentYear: Int, institutionId: String): ValidationContext = {
    ValidationContext(Some(Institution.empty.copy(id = institutionId)), Some(currentYear))
  }

  private def sendValidationStats(validationStats: ActorRef, institutionId: String, year: Int, submittedCount: Int): Unit = {
    validationStats ! AddSubmissionSubmittedTotal(submittedCount, SubmissionId(institutionId, year.toString, 1))
  }
}
