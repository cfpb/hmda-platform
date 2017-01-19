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
    Await.result(repository.createSchema(), timeout)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    Await.result(repository.dropSchema(), timeout)
  }

  val probe = TestProbe()

  "Filing database projection" must {
    val projection = createHmdaFilingDBProjection(system, "2017")
    "create schema" in {
      Await.result(repository.dropSchema(), timeout)
      probe.send(projection, CreateSchema)
      probe.expectMsg(FilingSchemaCreated())
    }
    "Insert records" in {
      val lar = larGen.sample.get
      probe.send(projection, LarValidated(lar))
      probe.expectMsg(LarInserted(1))
    }
  }

}
