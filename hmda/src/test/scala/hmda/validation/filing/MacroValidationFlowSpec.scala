package hmda.validation.filing

import akka.stream.scaladsl.Source
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.filing.lar.LarCsvParser
import MacroValidationFlow._
import hmda.util.SourceUtils._
import hmda.model.filing.lar.enums.{
  ApplicationApprovedButNotAccepted,
  LoanOriginated
}
import hmda.model.validation.{EmptyMacroValidationError, MacroValidationError}

class MacroValidationFlowSpec
    extends AsyncWordSpec
    with MustMatchers
    with BeforeAndAfterAll {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  override def afterAll(): Unit = {
    system.terminate()
  }

  val fileSource = scala.io.Source.fromURL(
    getClass.getResource("/clean_file_1000_rows_Bank0_syntax_validity.txt"))

  val lars = fileSource
    .getLines()
    .drop(1)
    .map(x => LarCsvParser(x).getOrElse(LoanApplicationRegister()))
    .toList

  val total = lars.size

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val loanOriginatedSource = source.map { lar =>
    val larAction = lar.action.copy(actionTakenType = LoanOriginated)
    lar.copy(action = larAction)
  }

  "Macro Validation" must {
    "count total number of LARs" in {
      count(source).map(t => t mustBe total)
    }

    "pass Q635" in {
      macroEdit(loanOriginatedSource, total, q635Ratio, q635Name).map(e =>
        e mustBe EmptyMacroValidationError())
    }
    "fail Q635" in {
      val q635Fail = loanOriginatedSource.take(200).map { lar =>
        val larAction =
          lar.action.copy(actionTakenType = ApplicationApprovedButNotAccepted)
        lar.copy(action = larAction)
      }

      val q635Source = loanOriginatedSource.drop(200) concat q635Fail
      count(q635Source).map(t => t mustBe total)
      macroEdit(q635Source, total, q635Ratio, q635Name).map(e =>
        e mustBe MacroValidationError("Q635"))
    }
  }

}
