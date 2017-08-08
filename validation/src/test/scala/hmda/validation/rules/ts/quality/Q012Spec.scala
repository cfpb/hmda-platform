package hmda.validation.rules.ts.quality

import akka.actor.{ ActorRef, ActorSystem }
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.ts.{ TransmittalSheet, TsGenerators }
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

import scala.concurrent.duration._
import hmda.validation.ValidationStats._

class Q012Spec extends AsyncWordSpec with MustMatchers with TsGenerators with BeforeAndAfterAll {
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

  "Q012" must {
    val currentYear = 2017
    val lastYear = currentYear - 1

    "be named Q012 when institution and year are present" in {
      val ctx = ValidationContext(Some(Institution.empty), Some(currentYear))
      Q012.inContext(ctx).name mustBe "Q012"
    }

    "be named empty when institution is not present" in {
      val ctx = ValidationContext(None, Some(currentYear))
      Q012.inContext(ctx).name mustBe "empty"
    }

    "be named empty when year is not present" in {
      val ctx = ValidationContext(Some(Institution.empty), None)
      Q012.inContext(ctx).name mustBe "empty"
    }

    "succeed when previous and current tax ID are equal" in {
      val instId = "instId1"
      val ctx = generateValidationContext(currentYear, instId)
      val taxId = Gen.alphaStr.sample.getOrElse("")
      sendValidationStats(validationStats, instId, 1, lastYear, taxId)
      Q012.inContext(ctx)(tsGenWithTaxId(taxId)).map(r => r mustBe a[Success])
    }

    "succeed when previous year is empty" in {
      val instId = "instId2"
      val ctx = generateValidationContext(currentYear, instId)
      val taxId = Gen.alphaStr.sample.getOrElse("")
      Q012.inContext(ctx)(tsGenWithTaxId(taxId)).map(r => r mustBe a[Success])
    }
    "fail when previous tax ID is different from current tax ID" in {
      val instId = "instId3"
      val ctx = generateValidationContext(currentYear, instId)
      val taxId = Gen.alphaStr.sample.getOrElse("")
      sendValidationStats(validationStats, instId, 1, lastYear, taxId)
      Q012.inContext(ctx)(tsGenWithTaxId(taxId + "will fail")).map(r => r mustBe a[Failure])
    }

  }

  private def tsGenWithTaxId(taxId: String): TransmittalSheet = {
    tsGen.sample.getOrElse(TransmittalSheet()).copy(taxId = taxId)
  }

  private def generateValidationContext(currentYear: Int, institutionId: String): ValidationContext = {
    ValidationContext(Some(Institution.empty.copy(id = institutionId)), Some(currentYear))
  }

  private def sendValidationStats(validationStats: ActorRef, institutionId: String, seqNr: Int, lastYear: Int, taxId: String): Unit = {
    validationStats ! AddSubmissionTaxId(taxId, SubmissionId(institutionId, lastYear.toString, 1))
  }
}
