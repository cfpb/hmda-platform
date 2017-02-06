package hmda.query.projections.filing

import akka.testkit.TestProbe
import hmda.model.fi.lar.LarGenerators
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec
import hmda.query.DbConfiguration
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.Await
import scala.concurrent.duration._
import hmda.query.projections.filing.HmdaFilingDBProjection._

class HmdaFilingDBProjectionSpec extends ActorSpec with DbConfiguration with BeforeAndAfterEach with LarGenerators {

  implicit val timeout = 5.seconds

  override def beforeEach(): Unit = {
    super.beforeEach()
    Await.result(larRepository.createSchema(), timeout)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    Await.result(larRepository.dropSchema(), timeout)
  }

  val probe = TestProbe()

  "Filing database projection" must {
    val projection = createHmdaFilingDBProjection(system, "2017")
    "create schema" in {
      Await.result(larRepository.dropSchema(), timeout)
      probe.send(projection, CreateSchema)
      probe.expectMsg(FilingSchemaCreated())
    }
    "Insert records" in {
      val lar = larGen.sample.get
      probe.send(projection, LarValidated(lar))
      probe.expectMsg(LarInserted(1))
    }
    "Delete records by respondent id" in {
      val lar1 = larGen.sample.get
      val lar2 = larGen.sample.get
      probe.send(projection, LarValidated(lar1))
      probe.expectMsg(LarInserted(1))
      probe.send(projection, LarValidated(lar2))
      probe.expectMsg(LarInserted(1))
      probe.send(projection, DeleteLars(lar1.respondentId))
      probe.expectMsg(LarsDeleted(lar1.respondentId))
    }
  }

}
