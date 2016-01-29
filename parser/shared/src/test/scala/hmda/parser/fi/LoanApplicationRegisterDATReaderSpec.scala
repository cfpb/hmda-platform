package hmda.parser.fi

import org.scalatest.{ MustMatchers, AsyncFlatSpec }

import scala.concurrent.{ Future, ExecutionContext }

class LoanApplicationRegisterDATReaderSpec extends AsyncFlatSpec with MustMatchers {

  override implicit def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val larsDAT = Seq(
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B                                                                                                                                                                                                                                                                            x",
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B                                                                                                                                                                                                                                                                            x",
    "201234567899ABCDEFGHIJKLMNOPQRSTUVWXY20130117432110000152013011906920060340100.01457432187654129000098701.0524B"
  )

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
