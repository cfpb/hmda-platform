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
  ApplicationWithdrawnByApplicant,
  FileClosedForIncompleteness,
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

  val incomeGreaterThan10 = source.map { lar =>
    lar.copy(income = "20")
  }

  "Macro Validation" must {
    "count total number of LARs" in {
      count(source).map(t => t mustBe total)
    }

    "pass Q635" in {
      macroEdit(loanOriginatedSource,
                total,
                q635Ratio,
                q635Name,
                applicationApprovedButNotAccepted).map(e =>
        e mustBe EmptyMacroValidationError())
    }

    "fail Q635" in {
      val totalFailing = 200
      val q635Fail = loanOriginatedSource.take(totalFailing).map { lar =>
        val larAction =
          lar.action.copy(actionTakenType = ApplicationApprovedButNotAccepted)
        lar.copy(action = larAction)
      }

      val q635Source = loanOriginatedSource.drop(totalFailing) concat q635Fail
      count(q635Source).map(t => t mustBe total)
      macroEdit(q635Source,
                total,
                q635Ratio,
                q635Name,
                applicationApprovedButNotAccepted).map(e =>
        e mustBe MacroValidationError(q635Name))
    }

    "pass Q636" in {
      macroEdit(loanOriginatedSource,
                total,
                q636Ratio,
                q636Name,
                applicationWithdrawnByApplicant).map(e =>
        e mustBe EmptyMacroValidationError())
    }

    "fail Q636" in {
      val totalFailing = 310
      val q636Fail = loanOriginatedSource.take(totalFailing).map { lar =>
        val larAction =
          lar.action.copy(actionTakenType = ApplicationWithdrawnByApplicant)
        lar.copy(action = larAction)
      }

      val q636Source = loanOriginatedSource.drop(totalFailing) concat q636Fail
      count(q636Source).map(t => t mustBe total)
      macroEdit(q636Source,
                total,
                q636Ratio,
                q636Name,
                applicationWithdrawnByApplicant).map(e =>
        e mustBe MacroValidationError(q636Name))

      1 mustBe 1
    }

    "pass Q637" in {
      macroEdit(loanOriginatedSource,
                total,
                q637Ratio,
                q637Name,
                fileClosedForIncompleteness).map(e =>
        e mustBe EmptyMacroValidationError())
    }

    "fail Q637" in {
      val totalFailing = 200
      val q637Fail = loanOriginatedSource.take(totalFailing).map { lar =>
        val larAction =
          lar.action.copy(actionTakenType = FileClosedForIncompleteness)
        lar.copy(action = larAction)
      }

      val q637Source = loanOriginatedSource.drop(totalFailing) concat q637Fail
      count(q637Source).map(t => t mustBe total)
      macroEdit(q637Source,
                total,
                q637Ratio,
                q637Name,
                fileClosedForIncompleteness).map(e =>
        e mustBe MacroValidationError(q637Name))
    }

    "pass Q640" in {
      macroEdit(incomeGreaterThan10,
                total,
                q640Ratio,
                q640Name,
                incomeLessThan10).map(e => e mustBe EmptyMacroValidationError())
    }

  }

}
