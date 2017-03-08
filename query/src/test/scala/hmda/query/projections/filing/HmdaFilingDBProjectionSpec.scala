package hmda.query.projections.filing

import akka.testkit.TestProbe
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec
import hmda.query.DbConfiguration
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }

import scala.concurrent.duration._
import hmda.query.projections.filing.HmdaFilingDBProjection._

class HmdaFilingDBProjectionSpec extends ActorSpec with DbConfiguration with BeforeAndAfterEach with BeforeAndAfterAll with LarGenerators {

  implicit val timeout = 5.seconds

  override def afterAll(): Unit = {
    super.afterAll()
    larRepository.config.db.close()
  }

  val probe = TestProbe()

  "Filing database projection" must {
    val projection = createHmdaFilingDBProjection(system, "2017")
    "create schema" in {
      probe.send(projection, CreateSchema)
      probe.expectMsg(FilingSchemaCreated())
    }
    "Insert records" in {
      val lar = sampleLar
      probe.send(projection, LarValidated(lar))
      probe.expectMsg(LarInserted(1))
    }
    "Delete records by respondent id" in {
      val lar1 = sampleLar
      val lar2 = sampleLar
      probe.send(projection, LarValidated(lar1))
      probe.expectMsg(LarInserted(1))
      probe.send(projection, LarValidated(lar2))
      probe.expectMsg(LarInserted(1))
      probe.send(projection, DeleteLars(lar1.respondentId))
      probe.expectMsg(LarsDeleted(lar1.respondentId))
    }
  }

}
