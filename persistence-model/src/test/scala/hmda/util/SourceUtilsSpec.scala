package hmda.util

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

import scala.util.Try

class SourceUtilsSpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll with SourceUtils {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val l5 = List(1, 2, 3, 4, 5)
  val l4 = List(1, 2, 3, 4)
  val l3 = List(1, 2, 3)

  "SourceUtils" must {
    "count the elements in a Source" in {
      val s5 = Source.fromIterator(() => l5.toIterator)
      val s4 = Source.fromIterator(() => l4.toIterator)
      val s3 = Source.fromIterator(() => l3.toIterator)
      count(s5).map(total => total mustBe 5)
      count(s4).map(total => total mustBe 4)
      count(s3).map(total => total mustBe 3)
    }

    "sum the elements in a Source" in {
      def itself(int: Int) = int
      val s5 = Source.fromIterator(() => l5.toIterator)
      val s4 = Source.fromIterator(() => l4.toIterator)
      val s3 = Source.fromIterator(() => l3.toIterator)
      sum(s5, itself).map(total => total mustBe 15)
      sum(s4, itself).map(total => total mustBe 10)
      sum(s3, itself).map(total => total mustBe 6)
    }

    val lars = List(
      LoanApplicationRegister(rateSpread = "1.0", loan = Loan("", "", 0, 0, 0, 0, 100)),
      LoanApplicationRegister(rateSpread = "2.0", loan = Loan("", "", 0, 0, 0, 0, 200)),
      LoanApplicationRegister(rateSpread = "3.0", loan = Loan("", "", 0, 0, 0, 0, 300)),
      LoanApplicationRegister(rateSpread = "4.0", loan = Loan("", "", 0, 0, 0, 0, 400)),
      LoanApplicationRegister(rateSpread = "10.0", loan = Loan("", "", 0, 0, 0, 0, 1000))
    )
    val source: Source[LoanApplicationRegister, NotUsed] = Source
      .fromIterator(() => lars.toIterator)

    def rateSpread(lar: LoanApplicationRegister): Double =
      Try(lar.rateSpread.toDouble).getOrElse(0)
    def loanAmount(lar: LoanApplicationRegister): Int = lar.loan.amount

    "calculate the mean of a list of loans" in {
      calculateMean(source, rateSpread).map(mean => mean mustBe 4.0)
      calculateMean(source, loanAmount).map(mean => mean mustBe 400.0)
    }

    "calculate the median of a list of loans" in {
      calculateMedian(source, rateSpread).map(median => median mustBe 3.0)
      calculateMedian(source, loanAmount).map(median => median mustBe 300.0)
    }

  }

  override def afterAll(): Unit = {
    system.terminate()
  }

}
