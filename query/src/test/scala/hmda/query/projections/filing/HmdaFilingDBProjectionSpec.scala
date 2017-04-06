package hmda.query.projections.filing

import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LarGenerators
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec

import scala.concurrent.duration._
import hmda.query.projections.filing.HmdaFilingDBProjection._

class HmdaFilingDBProjectionSpec extends ActorSpec with LarGenerators {

  implicit val timeout = 5.seconds

  val probe = TestProbe()

  "Filing database projection" must {
    val projection = createHmdaFilingDBProjection(system, "2017")
    "create schema" in {
      probe.send(projection, CreateSchema)
      probe.expectMsg(FilingSchemaCreated())
    }
    "Insert records" in {
      val lar = sampleLar
      probe.send(projection, LarValidated(lar, SubmissionId()))
      probe.expectMsg(LarInserted(1))
    }
    "Delete records by institution id" in {
      val lar1 = sampleLar
      val lar2 = sampleLar
      val testInstId = "test"
      probe.send(projection, LarValidated(lar1, SubmissionId().copy(institutionId = testInstId)))
      probe.expectMsg(LarInserted(1))
      probe.send(projection, LarValidated(lar2, SubmissionId()))
      probe.expectMsg(LarInserted(1))
      probe.send(projection, DeleteLars(testInstId))
      probe.expectMsg(LarsDeleted(testInstId))
    }
  }

}
