package hmda.validation.filing

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.model.validation.{
  EmptyMacroValidationError,
  MacroValidationError,
  ValidationError
}
import hmda.util.SourceUtils._
import hmda.validation.{AS, EC, MAT}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object MacroValidationFlow {

  type LarPredicate = LoanApplicationRegister => Boolean

  final val q634Name = "Q634"
  final val q635Name = "Q635"
  final val q636Name = "Q636"
  final val q637Name = "Q637"
  final val q638Name = "Q638"
  final val q639Name = "Q639"
  final val q640Name = "Q640"

  val config = ConfigFactory.load()
  final val q635Ratio = config.getDouble("edits.Q635.ratio")
  final val q636Ratio = config.getDouble("edits.Q636.ratio")
  final val q637Ratio = config.getDouble("edits.Q637.ratio")
  final val q640Ratio = config.getDouble("edits.Q640.ratio")
  final val q640Income = config.getInt("edits.Q640.income")

//  def macroValidation[mat: MAT, ec: EC](
//      source: Source[LoanApplicationRegister, NotUsed]) = {
//    val fTotal = source.via(total).runWith(Sink.head)
//
//    for {
//      total <- fTotal
//      q635 <- Q635(source, total)
//      q636 <- Q636(source, total)
//    } yield {
//      (q635, q636)
//    }
//  }

  def macroEdit[as: AS, mat: MAT, ec: EC](
      source: Source[LoanApplicationRegister, NotUsed],
      total: Int,
      editRatio: Double,
      editName: String,
      predicate: LarPredicate): Future[ValidationError] = {
    for {
      editCount <- count(
        source
          .filter(predicate))
    } yield {
      val ratio = editCount.toDouble / total.toDouble
      if (ratio > editRatio) MacroValidationError(editName)
      else EmptyMacroValidationError()
    }
  }

  //Q634
  def homePurchaseLoanOriginated: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == LoanOriginated &&
        lar.loan.loanPurpose == HomePurchase

  //Q635
  def applicationApprovedButNotAccepted: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == ApplicationApprovedButNotAccepted

  //Q636
  def applicationWithdrawnByApplicant: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == ApplicationWithdrawnByApplicant

  //Q637
  def fileClosedForIncompleteness: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == FileClosedForIncompleteness

  //Q638_1
  def loanOriginated: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == LoanOriginated

  //Q638_2
  def notRequested: LarPredicate =
    (lar: LoanApplicationRegister) =>
      List(
        LoanOriginated,
        ApplicationApprovedButNotAccepted,
        ApplicationDenied,
        ApplicationWithdrawnByApplicant,
        FileClosedForIncompleteness,
        PurchasedLoan
      ).contains(lar.action.actionTakenType)

  //Q639
  def preapprovalRequested: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.preapproval == PreapprovalRequested

  //Q640
  def incomeLessThan10: LarPredicate =
    (lar: LoanApplicationRegister) => {
      val income: Int = Try(lar.income.toInt) match {
        case Success(i) => i
        case Failure(_) => 0
      }
      income < q640Income
    }

}
