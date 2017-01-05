package hmda.query.projections.institutions

import akka.testkit.TestProbe
import hmda.model.institution.InstitutionGenerators
import hmda.persistence.model.ActorSpec
import hmda.query.projections.institutions.InstitutionDBProjection._
import hmda.persistence.messages.events.institutions.InstitutionEvents._
import hmda.query.DbConfiguration
import org.scalatest.BeforeAndAfterEach
import scala.concurrent.duration._
import scala.concurrent.Await

class InstitutionDBProjectionSpec extends ActorSpec with DbConfiguration with BeforeAndAfterEach {

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

  "Institution database projection" must {
    val projection = createInstitutionDBProjection(system)
    "create schema" in {
      Await.result(repository.dropSchema(), timeout)
      probe.send(projection, CreateSchema)
      probe.expectMsg(InstitutionSchemaCreated())
    }
    "Insert records" in {
      val i = InstitutionGenerators.institutionGen.sample.get
      probe.send(projection, InstitutionCreated(i))
      probe.expectMsg(InstitutionInserted(1))
    }
  }
}
