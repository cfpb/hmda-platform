package hmda.validation.filing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._
import hmda.model.filing.lar.{ LarAction, LoanApplicationRegister }
import hmda.model.filing.submission.SubmissionId
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.validation.{ EmptyMacroValidationError, MacroValidationError }
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.util.SourceUtils._
import hmda.validation.filing.MacroValidationFlow._
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

import scala.concurrent.Future

class MacroValidationFlowSpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll {

  implicit val system       = ActorSystem().toTyped
  implicit val materializer = Materializer(system)
  implicit val ec           = system.executionContext

  override def afterAll(): Unit =
    system.terminate()

  val fileSource = scala.io.Source.fromURL(getClass.getResource("/clean_file_1000_rows_Bank0_syntax_validity.txt"))

  val lars = fileSource
    .getLines()
    .drop(1)
    .map(x => LarCsvParser(x).getOrElse(LoanApplicationRegister()))
    .toList

  val ts = fileSource
    .getLines()
    .take(1)
    .map(x => TsCsvParser(x).getOrElse(TransmittalSheet()))
    .toList

  val total = lars.size

  val source: Source[LoanApplicationRegister, NotUsed] = Source
    .fromIterator(() => lars.toIterator)

  val tsSource: Source[TransmittalSheet, NotUsed] = Source
    .fromIterator(() => ts.toIterator)

  def fTotal: Future[Int] = count(source)

  val loanOriginatedSource = source.map { lar =>
    val larAction = lar.action.copy(actionTakenType = LoanOriginated)
    lar.copy(action = larAction)
  }

  val incomeGreaterThan10 = source.map(lar => lar.copy(income = "20"))

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

      val originatedSource = homePurchaseSource.take(97).map { lar =>
        val originated = lar.action.copy(actionTakenType = LoanOriginated)
        lar.copy(action = originated)
      }

      val q634Source = homePurchaseSource.drop(97) concat originatedSource
      Q634(q634Source).map(e => e mustBe MacroValidationError(q634Name))
    }

    "pass Q635" in {
      fTotal.flatMap { totalCount =>
        macroEdit(loanOriginatedSource, totalCount, q635Ratio, q635Name, applicationApprovedButNotAccepted).map(e =>
          e mustBe EmptyMacroValidationError()
        )
      }

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
      fTotal.flatMap { totalCount =>
        macroEdit(q635Source, totalCount, q635Ratio, q635Name, applicationApprovedButNotAccepted).map(e =>
          e mustBe MacroValidationError(q635Name)
        )
      }
    }

    "pass Q636" in {
      fTotal.flatMap { totalCount =>
        macroEdit(loanOriginatedSource, totalCount, q636Ratio, q636Name, applicationWithdrawnByApplicant).map(e =>
          e mustBe EmptyMacroValidationError()
        )
      }

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
      fTotal.flatMap { totalCount =>
        macroEdit(q636Source, totalCount, q636Ratio, q636Name, applicationWithdrawnByApplicant).map(e =>
          e mustBe MacroValidationError(q636Name)
        )
      }
    }

    "pass Q637" in {
      fTotal.flatMap { totalCount =>
        macroEdit(loanOriginatedSource, totalCount, q637Ratio, q637Name, fileClosedForIncompleteness).map(e =>
          e mustBe EmptyMacroValidationError()
        )
      }

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
      fTotal.flatMap { totalCount =>
        macroEdit(q637Source, totalCount, q637Ratio, q637Name, fileClosedForIncompleteness).map(e =>
          e mustBe MacroValidationError(q637Name)
        )
      }
    }

    "pass Q638" in {
      val q638Source = source.map(lar => lar.copy(action = LarAction(actionTakenType = LoanOriginated)))
      Q638(q638Source)
        .map(e => e mustBe EmptyMacroValidationError())
    }

    "fail Q638" in {
      val originated = source
        .take(100)
        .map(lar => lar.copy(action = LarAction(actionTakenType = LoanOriginated)))
      val rest = source
        .drop(100)
        .map(lar => lar.copy(action = LarAction(actionTakenType = ApplicationApprovedButNotAccepted)))

      val q638Fail = originated concat rest

      Q638(q638Fail).map(e => e mustBe MacroValidationError("Q638"))
    }

    "pass Q639" in {
      Q639(source).map(e => e mustBe EmptyMacroValidationError())
    }

    "fail Q639" in {
      val action1 = LarAction(preapproval = PreapprovalRequested, actionTakenType = PurchasedLoan)
      val extraLar = larGen.sample
        .getOrElse(LoanApplicationRegister())
        .copy(action = action1)
      val q639Fail = source.map { lar =>
        val larAction = lar.action.copy(actionTakenType = PurchasedLoan, preapproval = PreapprovalRequested)
        lar.copy(action = larAction)
      } concat Source.fromIterator(() => List(extraLar).toIterator)

      Q639(q639Fail).map(e => e mustBe MacroValidationError("Q639"))
    }

    "pass Q640" in {
      fTotal.flatMap { totalCount =>
        macroEdit(incomeGreaterThan10, totalCount, q640Ratio, q640Name, incomeLessThan10).map(e => e mustBe EmptyMacroValidationError())
      }

    }

    "fail Q640" in {
      val totalFailing = 210
      val q640Fail = source.take(totalFailing).map { lar =>
        val failIncome = "5"
        lar.copy(income = failIncome)
      }

      val q640Source = source.drop(totalFailing) concat q640Fail
      count(q640Source).map(t => t mustBe total)
      fTotal.flatMap { totalCount =>
        macroEdit(q640Source, totalCount, q640Ratio, q640Name, incomeLessThan10)
          .map(e => e mustBe MacroValidationError(q640Name))
      }

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
      macroValidation(failSource, tsSource, SubmissionId("somelei-2018-33"))
        .map(xs =>
          xs mustBe List(
            MacroValidationError(q635Name),
            MacroValidationError(q636Name),
            MacroValidationError(q638Name)
          )
        )

    }

  }
}