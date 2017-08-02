package hmda.query.repository.institutions

import hmda.model.institution.InstitutionGenerators
import hmda.query.DbConfiguration._

import scala.concurrent.duration._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers }

import scala.concurrent.Await

class InstitutionComponentSpec extends AsyncWordSpec with MustMatchers with InstitutionComponent with BeforeAndAfterAll {

  import InstitutionConverter._

  val timeout = 5.seconds

  val repository = new InstitutionRepository(config)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(repository.createSchema(), timeout)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    Await.result(repository.dropSchema(), timeout)
  }

  "Institution Repository" must {
    "insert new records" in {
      val i = InstitutionGenerators.sampleInstitution
      repository.insertOrUpdate(i).map(x => x mustBe 1)
    }
    "modify records and read them back" in {
      val i = InstitutionGenerators.sampleInstitution.copy(cra = false)
      repository.insertOrUpdate(i).map(x => x mustBe 1)
      val modified = i.copy(cra = true)
      repository.insertOrUpdate(modified).map(x => x mustBe 1)
      repository.findById(i.id).map {
        case Some(x) => x.cra mustBe modified.cra
        case None => fail
      }
    }
    "delete record" in {
      val i = InstitutionGenerators.sampleInstitution
      repository.insertOrUpdate(i).map(x => x mustBe 1)
      repository.findById(i.id).map {
        case Some(x) => succeed
        case None => fail
      }
      repository.deleteById(i.id).map(x => x mustBe 1)
      repository.findById(i.id).map {
        case Some(x) => fail
        case None => succeed
      }
    }
  }
}
