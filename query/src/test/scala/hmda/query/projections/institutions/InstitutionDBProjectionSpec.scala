package hmda.query.projections.institutions

import akka.testkit.TestProbe
import hmda.model.institution.InstitutionGenerators
import hmda.persistence.model.ActorSpec
import hmda.query.projections.institutions.InstitutionDBProjection._
import hmda.persistence.messages.events.institutions.InstitutionEvents._
import scala.concurrent.duration._

class InstitutionDBProjectionSpec extends ActorSpec {

  implicit val timeout = 5.seconds

  val probe = TestProbe()

  val projection = createInstitutionDBProjection(system)

  "Institution database projection" must {
    "create schema" in {
      probe.send(projection, CreateSchema)
      probe.expectMsg(InstitutionSchemaCreated())
    }
    "Insert records" in {
      val i = InstitutionGenerators.sampleInstitution
      probe.send(projection, InstitutionCreated(i))
      probe.expectMsg(InstitutionInserted(1))
    }
    "Delete Schema" in {
      probe.send(projection, DeleteSchema)
      probe.expectMsg(InstitutionSchemaDeleted())
    }
  }
}
