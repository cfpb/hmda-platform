package hmda.query.projections.institutions

import akka.testkit.TestActorRef
import hmda.model.institution.InstitutionGenerators
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.model.AsyncActorSpec
import hmda.query.dao.institutions.InstitutionDAO
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

class InstitutionDBProjectionSpec extends AsyncActorSpec with InstitutionDAO with H2Driver {

  var actorRef: TestActorRef[InstitutionDBProjection] = _
  var db: Database = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    actorRef = TestActorRef[InstitutionDBProjection]
    val actor = actorRef.underlyingActor
    db = actor.db
    db.run(createSchema())
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

      db.run(get(i1.id)).map { x =>
        x.get.id mustBe i1.id
      }

      db.run(get(i2.id)).map { x =>
        x.get.id mustBe i2.id
      }

      db.run(get(i3.id)).map { x =>
        x.get.id mustBe i3.id
      }

    }
    "Modify record when receiving modified event" in {
      actorRef ! InstitutionModified(i4)

      db.run(get(i4.id)).map { x =>
        x.get.id mustBe i3.id
      }

    }
  }

}
