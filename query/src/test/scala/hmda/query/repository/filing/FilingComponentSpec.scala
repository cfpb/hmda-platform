package hmda.query.repository.filing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep, RunnableGraph, Sink }
import hmda.model.fi.lar.LarGenerators
import hmda.query.DbConfiguration
import hmda.query.model.filing.{ LoanApplicationRegisterQuery, ModifiedLoanApplicationRegister }

import scala.concurrent.duration._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

import scala.concurrent.{ Await, Future }

class FilingComponentSpec extends AsyncWordSpec with MustMatchers with FilingComponent with DbConfiguration with BeforeAndAfterAll with LarGenerators {

  import LarConverter._
  import config.profile.api._

  val duration = 5.seconds

  val repository = new LarRepository(config)
  val larTotalRepository = new LarTotalRepository(config)
  val modifiedLarRepository = new ModifiedLarRepository(config)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  override def beforeAll(): Unit = {
    super.beforeAll()
    dropAllObjects()
    Await.result(repository.createSchema(), duration)
    Await.result(modifiedLarRepository.createSchema(), duration)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dropAllObjects()
    repository.config.db.close()
    system.terminate()
  }

  private def dropAllObjects() = {
    val db = repository.config.db
    val dropAll = sqlu"""DROP ALL OBJECTS"""
    Await.result(db.run(dropAll), duration)
  }

  "LAR Repository" must {
    "insert new records" in {
      val respId = "resp1"
      val year1 = 2016
      val year2 = 2017
      val lar1 = implicitly[LoanApplicationRegisterQuery](larGen.sample.get.copy(respondentId = "resp1")).copy(period = year1.toString)
      val lar2 = implicitly[LoanApplicationRegisterQuery](larGen.sample.get.copy(respondentId = "resp1")).copy(period = year2.toString)
      val lar3 = implicitly[LoanApplicationRegisterQuery](larGen.sample.get.copy(respondentId = "resp1")).copy(period = year2.toString)
      repository.insertOrUpdate(lar1).map(x => x mustBe 1)
      repository.insertOrUpdate(lar2).map(x => x mustBe 1)
      repository.insertOrUpdate(lar3).map(x => x mustBe 1)
      larTotalRepository.count("resp1").map(x => x mustBe Some(3))
      larTotalRepository.countInYear("resp1", 2017).map(x => x mustBe Some(2))
      modifiedLarRepository.findByRespondentId(lar1.respondentId).map {
        case xs: Seq[ModifiedLoanApplicationRegister] => xs.head.respondentId mustBe lar1.respondentId
      }
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

    "Stream rows for a specific respondent id" in {
      val respId = "resp2"
      val p = ""
      val lar1 = larGen.sample.get.copy(respondentId = respId)
      val lar2 = larGen.sample.get.copy(respondentId = respId)
      val lar3 = larGen.sample.get.copy(respondentId = respId)
      val lar4 = larGen.sample.get.copy(respondentId = "resp3")
      repository.insertOrUpdate(lar1)
      repository.insertOrUpdate(lar2)
      repository.insertOrUpdate(lar3)
      repository.insertOrUpdate(lar4)
      larTotalRepository.count("resp2").map(x => x mustBe Some(3))
      larTotalRepository.count("resp3").map(x => x mustBe Some(1))

      val lars = modifiedLarRepository.findByRespondentIdSource(respId, p)
      val count = Flow[ModifiedLoanApplicationRegister].map(_ => 1)
      val sum: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

      val counterGraph: RunnableGraph[Future[Int]] =
        lars
          .via(count)
          .toMat(sum)(Keep.right)

      val sumF: Future[Int] = counterGraph.run()
      val result = Await.result(sumF, duration)
      result mustBe 3

    }

  }

}

