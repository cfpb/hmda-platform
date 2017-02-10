package hmda.query.repository.filing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Keep, RunnableGraph, Sink }
import hmda.model.fi.lar.LarGenerators
import hmda.query.DbConfiguration
import hmda.query.model.filing.{ LoanApplicationRegisterQuery, ModifiedLoanApplicationRegister }

import scala.concurrent.duration._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers }

import scala.concurrent.{ Await, Future }

class FilingComponentSpec extends AsyncWordSpec with MustMatchers with FilingComponent with DbConfiguration with BeforeAndAfterEach with BeforeAndAfterAll with LarGenerators {

  import LarConverter._

  val duration = 5.seconds

  val larRepository = new LarRepository(config)
  val larTotalRepository = new LarTotalRepository(config)
  val modifiedLarRepository = new ModifiedLarRepository(config)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  override def afterAll(): Unit = {
    super.afterAll()
    dropSchema()
    system.terminate()
  }

  private def dropSchema(): Unit = {
    Await.result(modifiedLarRepository.dropSchema(), duration)
    Await.result(larTotalRepository.dropSchema(), duration)
    Await.result(larRepository.dropSchema(), duration)
    larRepository.config.db.close()
  }

  "LAR Repository" must {
    "insert new records" in {
      val lar1 = larGen.sample.get.copy(respondentId = "resp1")
      val lar2 = larGen.sample.get.copy(respondentId = "resp1")
      larRepository.insertOrUpdate(lar1).map(x => x mustBe 1)
      larRepository.insertOrUpdate(lar2).map(x => x mustBe 1)
      larTotalRepository.count("resp1").map(x => x mustBe Some(2))
      modifiedLarRepository.findByRespondentId(lar1.respondentId).map {
        case xs: Seq[ModifiedLoanApplicationRegister] => xs.head.respondentId mustBe lar1.respondentId
      }
    }
    "modify records and read them back" in {
      val lar: LoanApplicationRegisterQuery = larGen.sample.get.copy(agencyCode = 3)
      larRepository.insertOrUpdate(lar).map(x => x mustBe 1)
      val modified = lar.copy(agencyCode = 7)
      larRepository.insertOrUpdate(modified).map(x => x mustBe 1)
      larRepository.findById(lar.id).map {
        case Some(x) => x.agencyCode mustBe 7
        case None => fail
      }
    }
    "delete record" in {
      val lar: LoanApplicationRegisterQuery = larGen.sample.get
      larRepository.insertOrUpdate(lar).map(x => x mustBe 1)
      larRepository.findById(lar.id).map {
        case Some(_) => succeed
        case None => fail
      }
      larRepository.deleteById(lar.id).map(x => x mustBe 1)
      larRepository.findById(lar.id).map {
        case Some(_) => fail
        case None => succeed
      }
    }
    "delete all records" in {
      val lar: LoanApplicationRegisterQuery = larGen.sample.get
      val lar2: LoanApplicationRegisterQuery = larGen.sample.get
      larRepository.insertOrUpdate(lar).map(x => x mustBe 1)
      larRepository.insertOrUpdate(lar2).map(x => x mustBe 1)
      larRepository.findById(lar.id).map {
        case Some(_) => succeed
        case None => fail
      }
      larRepository.deleteAll.map(x => x mustBe 1)
      larRepository.findById(lar.id).map {
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
      larRepository.insertOrUpdate(lar1)
      larRepository.insertOrUpdate(lar2)
      larRepository.insertOrUpdate(lar3)
      larRepository.insertOrUpdate(lar4)
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

