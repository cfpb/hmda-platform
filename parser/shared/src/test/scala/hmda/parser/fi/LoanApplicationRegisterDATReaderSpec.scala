package hmda.parser.fi

import hmda.parser.test.FITestData
import org.scalatest.{ MustMatchers, AsyncFlatSpec }

import scala.concurrent.{ Future, ExecutionContext }

class LoanApplicationRegisterDATReaderSpec extends AsyncFlatSpec with MustMatchers {

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  import FITestData._

  val lars = larsDAT.map(line => LoanApplicationRegisterDATReader(line))

  "LAR Parser" should "parse correct number of LARs" in {
    val result = Future(lars.size)
    result.map(x => x mustBe 3)
  }

  it should "parse all LAR code == 2" in {
    val ids: Future[Seq[Int]] = for {
      lar <- Future(lars)
      id = lar.map(l => l.id)
    } yield id

    ids.map(s => s mustBe List(2, 2, 2))
  }

}
