package hmda.validation.filing

import akka.stream.scaladsl.Source
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, MustMatchers}
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.filing.lar.{LarAction, LoanApplicationRegister}
import hmda.parser.filing.lar.LarCsvParser
import MacroValidationFlow._
import hmda.util.SourceUtils._
import hmda.model.filing.lar.enums._
import hmda.model.validation.{EmptyMacroValidationError, MacroValidationError}
import hmda.model.filing.lar.LarGenerators._

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

    "pass Q634" in {
      Q634(source).map(e => e mustBe EmptyMacroValidationError())
    }

    "fail Q634" in {
      val homePurchaseSource = source.take(100).map { lar =>
        val homePurchaseLoan = lar.loan.copy(loanPurpose = HomePurchase)
        val approved =
          lar.action.copy(actionTakenType = ApplicationApprovedButNotAccepted)
        lar.copy(loan = homePurchaseLoan, action = approved)
      }

      val originatedSource = homePurchaseSource.take(30).map { lar =>
        val originated = lar.action.copy(actionTakenType = LoanOriginated)
        lar.copy(action = originated)
      }

      val q634Source = homePurchaseSource.drop(30) concat originatedSource
      Q634(q634Source).map(e => e mustBe MacroValidationError(q634Name))
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

    "pass Q638" in {
      val q638Source = source.map(lar =>
        lar.copy(action = LarAction(actionTakenType = LoanOriginated)))
      Q638(q638Source)
        .map(e => e mustBe EmptyMacroValidationError())
    }

    "fail Q638" in {
      val originated = source
        .take(100)
        .map(lar =>
          lar.copy(action = LarAction(actionTakenType = LoanOriginated)))
      val rest = source
        .drop(100)
        .map(lar =>
          lar.copy(action =
            LarAction(actionTakenType = ApplicationApprovedButNotAccepted)))

      val q638Fail = originated concat rest

      Q638(q638Fail).map(e => e mustBe MacroValidationError("Q638"))
    }

    "pass Q639" in {
      Q639(source).map(e => e mustBe EmptyMacroValidationError())
    }

    "fail Q639" in {
      val action1 = LarAction(preapproval = PreapprovalRequested,
                              actionTakenType = PurchasedLoan)
      val extraLar = larGen.sample
        .getOrElse(LoanApplicationRegister())
        .copy(action = action1)
      val q639Fail = source.map { lar =>
        val larAction = lar.action.copy(actionTakenType = PurchasedLoan,
                                        preapproval = PreapprovalRequested)
        lar.copy(action = larAction)
      } concat Source.fromIterator(() => List(extraLar).toIterator)

      Q639(q639Fail).map(e => e mustBe MacroValidationError("Q639"))
    }

    "pass Q640" in {
      macroEdit(incomeGreaterThan10,
                total,
                q640Ratio,
                q640Name,
                incomeLessThan10).map(e => e mustBe EmptyMacroValidationError())
    }

    "fail Q640" in {
      val totalFailing = 210
      val q640Fail = source.take(totalFailing).map { lar =>
        val failIncome = "5"
        lar.copy(income = failIncome)
      }

      val q640Source = source.drop(totalFailing) concat q640Fail
      count(q640Source).map(t => t mustBe total)
      macroEdit(q640Source, total, q640Ratio, q640Name, incomeLessThan10).map(
        e => e mustBe MacroValidationError(q640Name))
    }

    "collect macro edits" in {
      val q635Failing = 200
      val q536Failing = 310
      val q635Fail = loanOriginatedSource.take(q635Failing).map { lar =>
        val larAction =
          lar.action.copy(actionTakenType = ApplicationApprovedButNotAccepted)
        lar.copy(action = larAction)
      }

      val q636Fail = loanOriginatedSource.take(q536Failing).map { lar =>
        val larAction =
          lar.action.copy(actionTakenType = ApplicationWithdrawnByApplicant)
        lar.copy(action = larAction)
      }

      val failSource = q635Fail concat q636Fail

      macroValidation(failSource).map(
        xs =>
          xs mustBe List(
            MacroValidationError(q635Name),
            MacroValidationError(q636Name),
            MacroValidationError(q637Name),
            MacroValidationError(q638Name),
            MacroValidationError(q640Name)
        ))

    }

  }
}
