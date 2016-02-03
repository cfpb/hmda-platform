package hmda.parser.fi.lar

import hmda.parser.util.FITestData
import org.scalatest.{ AsyncFlatSpec, MustMatchers }

import scala.concurrent.{ ExecutionContext, Future }

class LarDatParserSpec extends AsyncFlatSpec with MustMatchers {

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  import FITestData._

  val lars = larsDAT.map(line => LarDatParser(line))

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
