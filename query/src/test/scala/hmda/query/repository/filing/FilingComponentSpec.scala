package hmda.query.repository.filing

import hmda.model.fi.lar.LarGenerators
import hmda.query.DbConfiguration
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.duration._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterEach, MustMatchers }

import scala.concurrent.Await

class FilingComponentSpec extends AsyncWordSpec with MustMatchers with FilingComponent with DbConfiguration with BeforeAndAfterEach with LarGenerators {

  import LarConverter._

  val timeout = 5.seconds

  val repository = new LarRepository(config)

  override def beforeEach(): Unit = {
    super.beforeEach()
    Await.result(repository.createSchema(), timeout)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    Await.result(repository.dropSchema(), timeout)
  }

  "LAR Repository" must {
    "insert new records" in {
      val lar = larGen.sample.get
      repository.insertOrUpdate(lar).map(x => x mustBe 1)
    }
    "modify records and read them back" in {
      val lar: LoanApplicationRegisterQuery = larGen.sample.get.copy(agencyCode = 3)
      repository.insertOrUpdate(lar).map(x => x mustBe 1)
      val modified = lar.copy(agencyCode = 7)
      repository.insertOrUpdate(modified).map(x => x mustBe 1)
      repository.findById(lar.id).map {
        case Some(x) => x.agencyCode mustBe 7
        case None => fail
      }
    }
    "delete record" in {
      val lar: LoanApplicationRegisterQuery = larGen.sample.get
      repository.insertOrUpdate(lar).map(x => x mustBe 1)
      repository.findById(lar.id).map {
        case Some(x) => succeed
        case None => fail
      }
      repository.deleteById(lar.id).map(x => x mustBe 1)
      repository.findById(lar.id).map {
        case Some(x) => fail
        case None => succeed
      }
    }
  }

}

