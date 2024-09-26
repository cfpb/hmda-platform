package hmda.validation.filing

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.model.filing.submission.SubmissionId
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.HUD
import hmda.model.validation.{ EmptyMacroValidationError, MacroValidationError, ValidationError }
import hmda.util.SourceUtils._
import hmda.utils.YearUtils.Period

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

object MacroValidationFlow {

  type LarPredicate = LoanApplicationRegister => Boolean
  type TsPredicate  = TransmittalSheet => Boolean

  final val q634Name = "Q634"
  final val q635Name = "Q635"
  final val q636Name = "Q636"
  final val q637Name = "Q637"
  final val q638Name = "Q638"
  final val q639Name = "Q639"
  final val q640Name = "Q640"
  final val q646Name = "Q646"
  final val q647Name = "Q647"

  final val exemptCode = "Exempt"

  val config              = ConfigFactory.load()
  final val q634Threshold = config.getInt("edits.Q634.threshold")
  final val q634Ratio     = config.getDouble("edits.Q634.ratio")
  final val q635Ratio     = config.getDouble("edits.Q635.ratio")
  final val q636Ratio     = config.getDouble("edits.Q636.ratio")
  final val q637Ratio     = config.getDouble("edits.Q637.ratio")
  final val q638Ratio     = config.getDouble("edits.Q638.ratio")
  final val q639Threshold = config.getInt("edits.Q639.threshold")
  final val q640Ratio     = config.getDouble("edits.Q640.ratio")
  final val q640Income    = config.getInt("edits.Q640.income")

  type Count = Int
  type MacroCheck =
    Source[LoanApplicationRegister, NotUsed] => Future[ValidationError]

  def selectedValidations(
                           totalCount: Int,
                           period: Period,
                           tsSource: Source[TransmittalSheet, NotUsed]
                         )(implicit system: ActorSystem[_], mat: Materializer, ec: ExecutionContext): List[MacroCheck] =
    period match {
      case Period(2018, _) =>
        List(
          Q634,
          Q635(totalCount),
          Q636(totalCount),
          Q637(totalCount),
          Q638,
          Q639,
          Q640
        )

      case Period(2019, _) =>
        List(
          Q634,
          Q635(totalCount),
          Q636(totalCount),
          Q637(totalCount),
          Q638,
          Q639,
          Q640,
          Q646,
          Q647(tsSource)
        )

      case Period(2020, _) =>
        List(
          Q634,
          Q635(totalCount),
          Q636(totalCount),
          Q637(totalCount),
          Q638,
          Q639,
          Q640,
          Q646,
          Q647(tsSource)
        )
      case Period(2021, _) =>
        List(
          Q634,
          Q635(totalCount),
          Q636(totalCount),
          Q637(totalCount),
          Q638,
          Q639,
          Q640,
          Q646,
          Q647(tsSource)
        )
      case Period(2022, _) =>
        List(
          Q634,
          Q635(totalCount),
          Q636(totalCount),
          Q637(totalCount),
          Q638,
          Q639,
          Q640,
          Q646,
          Q647(tsSource)
        )
      case Period(2023, _) =>
        List(
          Q634,
          Q635(totalCount),
          Q636(totalCount),
          Q637(totalCount),
          Q638,
          Q639,
          Q640,
          Q646,
          Q647(tsSource)
        )
      case Period(2024, _) =>
        List(
          Q634,
          Q635(totalCount),
          Q636(totalCount),
          Q637(totalCount),
          Q638,
          Q639,
          Q640,
          Q646,
          Q647(tsSource)
        )
      case Period(2025, _) =>
        List(
          Q634,
          Q635(totalCount),
          Q636(totalCount),
          Q637(totalCount),
          Q638,
          Q639,
          Q640
        )
      case _ =>
        List(
        )
    }

  def macroValidation(
                       larSource: Source[LoanApplicationRegister, NotUsed],
                       tsSource: Source[TransmittalSheet, NotUsed],
                       submissionId: SubmissionId
                     )(implicit system: ActorSystem[_], mat: Materializer, ec: ExecutionContext): Future[List[ValidationError]] = {
    val fTotal: Future[Int] = count(larSource)

    fTotal.flatMap { totalCount =>
      val validations: List[MacroCheck] =
        selectedValidations(totalCount, submissionId.period, tsSource)
      val fErrors: Future[List[ValidationError]] =
        Future.sequence(validations.map(eachFn => eachFn(larSource)))
      fErrors.map(errors => errors.filter(e => e != EmptyMacroValidationError()))
    }
  }

  def macroEdit(
                 source: Source[LoanApplicationRegister, NotUsed],
                 totalCount: Int,
                 editRatio: Double,
                 editName: String,
                 predicate: LarPredicate
               )(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] =
    count(source.filter(predicate)).map { editCount =>
      val ratio = editCount.toDouble / totalCount.toDouble
      if (ratio > editRatio) MacroValidationError(editName)
      else EmptyMacroValidationError()
    }

  def macroEditAny(
                    source: Source[LoanApplicationRegister, NotUsed],
                    editName: String,
                    predicate: LarPredicate
                  )(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] =
    count(source.filter(predicate))
      .map(editCount =>
        if (editCount != 0) MacroValidationError(editName)
        else EmptyMacroValidationError()
      )

  def Q634(
            source: Source[LoanApplicationRegister, NotUsed]
          )(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] = {

    val countPredicateF  = count(source.filter(homePurchaseLoanOriginated))
    val countComparisonF = count(source.filter(homePurchasedApp))

    for {
      countPredicate  <- countPredicateF
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

  def Q635(
            totalCount: Int
          )(source: Source[LoanApplicationRegister, NotUsed])(implicit system: ActorSystem[_], ec: ExecutionContext): Future[ValidationError] =
    macroEdit(source, totalCount, q635Ratio, q635Name, applicationApprovedButNotAccepted)

  def Q636(
            totalCount: Int
          )(source: Source[LoanApplicationRegister, NotUsed])(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] =
    macroEdit(source, totalCount, q636Ratio, q636Name, applicationWithdrawnByApplicant)

  def Q637(
            totalCount: Int
          )(source: Source[LoanApplicationRegister, NotUsed])(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] =
    macroEdit(source, totalCount, q637Ratio, q637Name, fileClosedForIncompleteness)

  def Q638(source: Source[LoanApplicationRegister, NotUsed])(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] = {

    val countPredicateF  = count(source.filter(loanOriginated))
    val countComparisonF = count(source.filter(notRequested))

    for {
      countPredicate  <- countPredicateF
      countComparison <- countComparisonF
      ratio           = countPredicate.toDouble / countComparison.toDouble
    } yield {
      if (ratio >= q638Ratio)
        EmptyMacroValidationError()
      else
        MacroValidationError(q638Name)
    }
  }

  def Q639(source: Source[LoanApplicationRegister, NotUsed])(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] =
    for {
      countRequested <- count(source.filter(preapprovalRequested))
      countDenied    <- count(source.filter(preapprovalDenied))
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

  def Q640(source: Source[LoanApplicationRegister, NotUsed])(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] =
    q640Total(source).flatMap(count => macroEdit(source, count, q640Ratio, q640Name, incomeLessThan10))

  def Q647(
            tsSource: Source[TransmittalSheet, NotUsed]
          )(larSource: Source[LoanApplicationRegister, NotUsed])(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] = {
    def countPredicateT: Future[Count] = count(tsSource.filter(isAgencyCodeSeven))
    def countPredicateL: Future[Count] = count(larSource.filter(exemptionTaken))
    for {
      countPredicateAgencyCode <- countPredicateT
      countPredicateExemptions <- countPredicateL
    } yield {
      if (countPredicateAgencyCode == 0) {
        EmptyMacroValidationError()
      } else if (countPredicateExemptions > 0) {
        MacroValidationError(q647Name)
      } else {
        EmptyMacroValidationError()
      }
    }
  }

  def Q646(source: Source[LoanApplicationRegister, NotUsed])(implicit mat: Materializer, ec: ExecutionContext): Future[ValidationError] =
    macroEditAny(source, q646Name, exemptionTaken)

  //Q646
  def isAgencyCodeSeven: TsPredicate =
    (ts: TransmittalSheet) => ts.agency == HUD

  //Q634
  def homePurchaseLoanOriginated: LarPredicate =
    (lar: LoanApplicationRegister) =>
      lar.action.actionTakenType == LoanOriginated &&
        lar.loan.loanPurpose == HomePurchase

  def homePurchasedApp: LarPredicate =
    (lar: LoanApplicationRegister) => lar.loan.loanPurpose == HomePurchase

  //Q635
  def applicationApprovedButNotAccepted: LarPredicate =
    (lar: LoanApplicationRegister) => lar.action.actionTakenType == ApplicationApprovedButNotAccepted

  //Q636
  def applicationWithdrawnByApplicant: LarPredicate =
    (lar: LoanApplicationRegister) => lar.action.actionTakenType == ApplicationWithdrawnByApplicant

  //Q637
  def fileClosedForIncompleteness: LarPredicate =
    (lar: LoanApplicationRegister) => lar.action.actionTakenType == FileClosedForIncompleteness

  //Q638_1
  def loanOriginated: LarPredicate =
    (lar: LoanApplicationRegister) => lar.action.actionTakenType == LoanOriginated

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
    (lar: LoanApplicationRegister) => lar.action.preapproval == PreapprovalRequested

  //Q639_2
  def preapprovalDenied: LarPredicate =
    (lar: LoanApplicationRegister) => lar.action.actionTakenType == PreapprovalRequestDenied

  //Q640
  def incomeLessThan10: LarPredicate =
    (lar: LoanApplicationRegister) => {
      Try(lar.income.toInt) match {
        case Success(i) => i < q640Income
        case Failure(_) => false
      }
    }

  def q640Total(source: Source[LoanApplicationRegister, NotUsed])(implicit mat: Materializer): Future[Int] =
    count(
      source.filter(lar => Try(lar.income.toInt).fold(_ => false, _ => true))
    )

  //Q646
  def exemptionTaken: LarPredicate =
    (lar: LoanApplicationRegister) => {
      lar.applicationSubmission == ApplicationSubmissionExempt || lar.ausResult.ausResult1 == AUSResultExempt || lar.ausResult.ausResult2 == AUSResultExempt || lar.ausResult.ausResult3 == AUSResultExempt || lar.ausResult.ausResult4 == AUSResultExempt ||
        lar.ausResult.ausResult5 == AUSResultExempt || lar.AUS.aus1 == AUSExempt || lar.AUS.aus2 == AUSExempt || lar.AUS.aus3 == AUSExempt || lar.AUS.aus4 == AUSExempt || lar.AUS.aus5 == AUSExempt ||
        lar.nonAmortizingFeatures.balloonPayment == BalloonPaymentExempt || lar.businessOrCommercialPurpose == ExemptBusinessOrCommercialPurpose || lar.applicant.creditScoreType == CreditScoreExempt || lar.coApplicant.creditScoreType == CreditScoreExempt ||
        lar.denial.denialReason1 == ExemptDenialReason || lar.denial.denialReason2 == ExemptDenialReason || lar.denial.denialReason3 == ExemptDenialReason || lar.denial.denialReason4 == ExemptDenialReason ||
        lar.nonAmortizingFeatures.interestOnlyPayments == InterestOnlyPaymentExempt || lar.lineOfCredit == ExemptLineOfCredit || lar.property.manufacturedHomeLandPropertyInterest == ManufacturedHomeLoanPropertyInterestExempt ||
        lar.property.manufacturedHomeSecuredProperty == ManufacturedHomeSecuredExempt || lar.reverseMortgage == ExemptMortgageType || lar.nonAmortizingFeatures.negativeAmortization == NegativeAmortizationExempt || lar.nonAmortizingFeatures.otherNonAmortizingFeatures == OtherNonAmortizingFeaturesExempt ||
        lar.payableToInstitution == PayableToInstitutionExempt || lar.loan.interestRate == exemptCode
    }

}