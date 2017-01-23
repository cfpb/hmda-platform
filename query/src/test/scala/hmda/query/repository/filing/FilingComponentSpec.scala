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
  val totalRepository = new LarTotalRepository(config)
  val modifiedLarRepository = new ModifiedLarRepository(config)

  override def beforeEach(): Unit = {
    super.beforeEach()
    Await.result(repository.createSchema(), timeout)
    Await.result(totalRepository.createSchema(), timeout)
  }

  override def afterEach(): Unit = {
    super.afterEach()
    Await.result(totalRepository.dropSchema(), timeout)
    Await.result(repository.dropSchema(), timeout)
  }

  "LAR Repository" must {
    "insert new records" in {
      val lar1 = larGen.sample.get.copy(respondentId = "resp1")
      val lar2 = larGen.sample.get.copy(respondentId = "resp1")
      repository.insertOrUpdate(lar1).map(x => x mustBe 1)
      repository.insertOrUpdate(lar2).map(x => x mustBe 1)
      totalRepository.count("resp1").map(x => x mustBe Some(2))
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
        case Some(_) => succeed
        case None => fail
      }
      repository.deleteById(lar.id).map(x => x mustBe 1)
      repository.findById(lar.id).map {
        case Some(_) => fail
        case None => succeed
      }
    }
    "delete all records" in {
      val lar: LoanApplicationRegisterQuery = larGen.sample.get
      val lar2: LoanApplicationRegisterQuery = larGen.sample.get
      repository.insertOrUpdate(lar).map(x => x mustBe 1)
      repository.insertOrUpdate(lar2).map(x => x mustBe 1)
      repository.findById(lar.id).map {
        case Some(_) => succeed
        case None => fail
      }
      repository.deleteAll.map(x => x mustBe 1)
      repository.findById(lar.id).map {
        case Some(_) => fail
        case None => succeed
      }
    }

  }

  //"LAR total repository" must {
  //  "Store total lar count" in {

  //    val lars = lar100ListGen.sample.get

  //    1 mustBe 1
  //    //val f = Await.result(Future.sequence(lars.map(lar => repository.insertOrUpdate(lar))), timeout)
  //  }
  //}

}

