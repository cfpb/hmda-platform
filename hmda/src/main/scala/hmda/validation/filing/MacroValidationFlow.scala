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
  final val q634Threshold = config.getInt("edits.Q634.threshold")
  final val q634Ratio = config.getDouble("edits.Q634.ratio")
  final val q635Ratio = config.getDouble("edits.Q635.ratio")
  final val q636Ratio = config.getDouble("edits.Q636.ratio")
  final val q637Ratio = config.getDouble("edits.Q637.ratio")
  final val q638Ratio = config.getDouble("edits.Q638.ratio")
  final val q639Threshold = config.getInt("edits.Q639.threshold")
  final val q640Ratio = config.getDouble("edits.Q640.ratio")
  final val q640Income = config.getInt("edits.Q640.income")

  def macroValidation[as: AS, mat: MAT, ec: EC](
      source: Source[LoanApplicationRegister, NotUsed]
  ): Future[List[ValidationError]] = {
    def fTotal: Future[Int] = count(source)
    for {
      q634 <- Q634(source)
      q635 <- macroEdit(source,
                        fTotal,
                        q635Ratio,
                        q635Name,
                        applicationApprovedButNotAccepted)
      q636 <- macroEdit(source,
                        fTotal,
                        q636Ratio,
                        q636Name,
                        applicationWithdrawnByApplicant)
      q637 <- macroEdit(source,
                        fTotal,
                        q637Ratio,
                        q637Name,
                        fileClosedForIncompleteness)
      q638 <- Q638(source)
      q639 <- Q639(source)
      q640 <- macroEdit(source, q640Total(source), q640Ratio, q640Name, incomeLessThan10)
    } yield {
      List(q634, q635, q636, q637, q638, q639, q640).filter(e =>
        e != EmptyMacroValidationError())
    }
  }

  def macroEdit[as: AS, mat: MAT, ec: EC](
      source: Source[LoanApplicationRegister, NotUsed],
      fTotal: Future[Int],
      editRatio: Double,
      editName: String,
      predicate: LarPredicate): Future[ValidationError] = {
    for {
      total <- fTotal
      editCount <- count(
        source
          .filter(predicate))
    } yield {
      val ratio = editCount.toDouble / total.toDouble
      if (ratio > editRatio) MacroValidationError(editName)
      else EmptyMacroValidationError()
    }
  }

  def Q634[as: AS, mat: MAT, ec: EC](
      source: Source[LoanApplicationRegister, NotUsed]
  ): Future[ValidationError] = {

    val countPredicateF = count(source.filter(homePurchaseLoanOriginated))
    val countComparisonF = count(source.filter(loanOriginated))

    for {
      countPredicate <- countPredicateF
      countComparison <- countComparisonF
    } yield {
      if (countPredicate <= q634Threshold) {
        EmptyMacroValidationError()
      } else {
        if (countPredicate <= q634Ratio * countComparison) {
          EmptyMacroValidationError()
        } else {
          MacroValidationError(q634Name)
        }
      }
    }
  }

  def Q638[as: AS, mat: MAT, ec: EC](
      source: Source[LoanApplicationRegister, NotUsed])
    : Future[ValidationError] = {

    val countPredicateF = count(source.filter(loanOriginated))
    val countComparisonF = count(source.filter(notRequested))

    for {
      countPredicate <- countPredicateF
      countComparison <- countComparisonF
      ratio = countPredicate.toDouble / countComparison.toDouble
    } yield {
      if (ratio >= q638Ratio)
        EmptyMacroValidationError()
      else
        MacroValidationError(q638Name)
    }
  }

  def Q639[as: AS, mat: MAT, ec: EC](
      source: Source[LoanApplicationRegister, NotUsed]
  ): Future[ValidationError] = {

    for {
      countRequested <- count(source.filter(preapprovalRequested))
      countDenied <- count(source.filter(preapprovalDenied))
    } yield {
      if (countRequested > 1000) {
        if (countDenied >= 1)
          EmptyMacroValidationError()
        else
          MacroValidationError(q639Name)
      } else {
        EmptyMacroValidationError()
      }
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

  //Q639_1
  def preapprovalRequested: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.preapproval == PreapprovalRequested

  //Q639_2
  def preapprovalDenied: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == PreapprovalRequestDenied

  //Q640
  def incomeLessThan10: LarPredicate =
    (lar: LoanApplicationRegister) => {
      Try(lar.income.toInt) match {
        case Success(i) => i < q640Income
        case Failure(_) => false
      }
    }
  
  def q640Total[as: AS, mat: MAT, ec: EC](source: Source[LoanApplicationRegister, NotUsed]): Future[Int] = {
    count(
      source.filter( lar =>
        Try(lar.income.toInt) match {
          case Success(_) => true
          case Failure(_) => false
        }
      )
    )
  }

}
