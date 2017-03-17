package hmda.query.projections.institutions

import akka.testkit.TestProbe
import hmda.model.institution.InstitutionGenerators
import hmda.persistence.model.ActorSpec
import hmda.query.projections.institutions.InstitutionDBProjection._
import hmda.persistence.messages.events.institutions.InstitutionEvents._
import hmda.query.DbConfiguration._

import scala.concurrent.duration._

class InstitutionDBProjectionSpec extends ActorSpec {

  implicit val timeout = 5.seconds

  val probe = TestProbe()

  "Institution database projection" must {
    val projection = createInstitutionDBProjection(system)
    "create schema" in {
      //Await.result(repository.dropSchema(), timeout)
      probe.send(projection, CreateSchema)
      probe.expectMsg(InstitutionSchemaCreated())
    }
    "Insert records" in {
      val i = InstitutionGenerators.sampleInstitution
      probe.send(projection, InstitutionCreated(i))
      probe.expectMsg(InstitutionInserted(1))
    }
  }
}
