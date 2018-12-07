package hmda.validation.filing

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import cats.data.OptionT
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.model.validation.MacroValidationError
import hmda.validation.{EC, MAT}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object MacroValidationFlow {

  type LarPredicate = LoanApplicationRegister => Boolean

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

  def Q635[mat: MAT, ec: EC](
      source: Source[LoanApplicationRegister, NotUsed],
      total: Int): OptionT[Future, MacroValidationError] = {
    val fValidation = for {
      q635 <- source
        .via(count(applicationApprovedButNotAccepted))
        .runWith(Sink.head)
      ratio = q635 / total
    } yield {
      if (ratio > 0.15) Some(MacroValidationError("Q635")) else None
    }
    OptionT(fValidation)
  }

  def Q636[mat: MAT, ec: EC](
      source: Source[LoanApplicationRegister, NotUsed],
      total: Int): OptionT[Future, MacroValidationError] = {
    val fValidation = for {
      q636 <- source
        .via(count(applicationWithdrawnByApplicant))
        .runWith(Sink.head)
      ratio = q636 / total
    } yield {
      if (ratio > 0.30) Some(MacroValidationError("Q636")) else None
    }
    OptionT(fValidation)
  }

  def count(
      larPredicate: LarPredicate): Flow[LoanApplicationRegister, Int, NotUsed] =
    Flow[LoanApplicationRegister]
      .filter(larPredicate)
      .fold(0)((acc, _) => acc + 1)

  //Q634
  private def homePurchaseLoanOriginated: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == LoanOriginated &&
        lar.loan.loanPurpose == HomePurchase

  //Q635
  private def applicationApprovedButNotAccepted: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == ApplicationApprovedButNotAccepted

  //Q636
  private def applicationWithdrawnByApplicant: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == ApplicationWithdrawnByApplicant

  //Q637
  private def fileClosedForIncompleteness: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == FileClosedForIncompleteness

  //Q638_1
  private def loanOriginated: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == LoanOriginated

  //Q638_2
  private def notRequested: LarPredicate =
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
  private def preapprovalRequested: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.preapproval == PreapprovalRequested

  //Q640
  private def incomeLessThan10: LarPredicate =
    (lar: LoanApplicationRegister) => {
      val income: Int = Try(lar.income.toInt) match {
        case Success(i) => i
        case Failure(_) => 0
      }
      income < 10
    }

}
