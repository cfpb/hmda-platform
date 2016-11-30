package hmda.query.repository.institutions

import hmda.model.institution.InstitutionGenerators
import hmda.query.DbConfiguration
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterEach, MustMatchers }

import scala.concurrent.Await
import scala.concurrent.duration._

class InstitutionsRepositorySpec extends AsyncWordSpec with DbConfiguration with MustMatchers with BeforeAndAfterEach {

  val timeout = 500.milliseconds
  val institutions = new InstitutionsRepository(config)

  import InstitutionConverter._

  override def beforeEach(): Unit = {
    super.beforeEach()
    Await.result(institutions.createSchema(), timeout)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    Await.result(institutions.dropSchema(), timeout)
  }

  "Institutions" must {
    "be inserted" in {
      val i = InstitutionGenerators.institutionGen.sample.get
      institutions.insertOrUpdate(i).map(x => x mustBe 1)
    }

    "be modified and read back" in {
      val i = InstitutionGenerators.institutionGen.sample.get.copy(cra = false)
      institutions.insertOrUpdate(i).map(x => x mustBe 1)
      val modified = i.copy(cra = true)
      institutions.update(modified).map(x => x mustBe 1)
      institutions.get(i.id).map {
        case Some(x) => x.cra mustBe modified.cra
        case None => fail
      }
    }
  }

}
