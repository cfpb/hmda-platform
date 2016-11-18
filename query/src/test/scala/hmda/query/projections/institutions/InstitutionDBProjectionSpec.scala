package hmda.query.projections.institutions

import akka.testkit.TestActorRef
import hmda.model.institution.InstitutionGenerators
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.AsyncActorSpec
import hmda.query.dao.institutions.InstitutionRepository
import hmda.query.model.institutions.InstitutionEntity
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

class InstitutionDBProjectionSpec extends AsyncActorSpec with InstitutionRepository with H2Driver {

  var actorRef: TestActorRef[InstitutionDBProjection] = _
  val repository = new InstitutionBaseRepository
  override val db: api.Database = Database.forConfig("h2mem")

  override def beforeAll(): Unit = {
    super.beforeAll()
    actorRef = TestActorRef[InstitutionDBProjection]
    val actor = actorRef.underlyingActor
    repository.createSchema()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    db.close()
  }

  val i1 = InstitutionGenerators.institutionGen.sample.get
  val i2 = InstitutionGenerators.institutionGen.sample.get
  val i3 = InstitutionGenerators.institutionGen.sample.get
  val i4 = i3.copy(cra = true)

  implicit val ec = system.dispatcher

  "Institution DB Projection" must {
    "Insert records when receiving inserted event" in {
      for (i <- List(i1, i2, i3)) {
        actorRef ! InstitutionCreated(i)
      }

      repository.find(i1.id).map { x =>
        x.getOrElse(InstitutionEntity()).id mustBe i1.id
      }

      repository.find(i2.id).map { x =>
        x.getOrElse(InstitutionEntity()).id mustBe i2.id
      }

      repository.find(i3.id).map { x =>
        x.getOrElse(InstitutionEntity()).id mustBe i3.id
      }
    }

    "Modify record when receiving modified event" in {
      actorRef ! InstitutionModified(i4)

      repository.find(i4.id).map { x =>
        x.getOrElse(InstitutionEntity()).id mustBe i3.id
      }

    }
  }

}
