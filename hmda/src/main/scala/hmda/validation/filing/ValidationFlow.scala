package hmda.validation.filing

import akka.NotUsed
import akka.stream.{ FlowShape, Materializer }
import akka.stream.scaladsl.{ Broadcast, Concat, Flow, GraphDSL }
import akka.util.ByteString
import cats.Semigroup
import hmda.model.filing.EditDescriptionLookup.config
import hmda.model.filing.{ EditDescriptionLookup, PipeDelimited }
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.{ TransmittalLar, TransmittalSheet }
import hmda.model.institution.Institution
import hmda.model.validation.{ LarValidationError, TsValidationError, ValidationError }
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.validation._
import hmda.validation.context.ValidationContext
import hmda.util.streams.FlowUtils._
import hmda.utils.YearUtils.Period
import hmda.validation.engine._
import hmda.util.conversion.ColumnDataFormatter

import scala.collection.immutable._
import scala.concurrent.{ ExecutionContext, Future }

object ValidationFlow extends ColumnDataFormatter {

  implicit val larSemigroup = new Semigroup[LoanApplicationRegister] {
    override def combine(x: LoanApplicationRegister, y: LoanApplicationRegister): LoanApplicationRegister =
      x
  }

  def validateHmdaFile(checkType: String, ctx: ValidationContext): Flow[ByteString, HmdaValidated[PipeDelimited], NotUsed] =
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast  = b.add(Broadcast[ByteString](2))
      val concat = b.add(Concat[HmdaValidated[PipeDelimited]](2))

      bcast.take(1) ~> validateTsFlow(checkType, ctx) ~> concat.in(0)
      bcast.drop(1) ~> validateLarFlow(checkType, ctx) ~> concat.in(1)

      FlowShape(bcast.in, concat.out)
    })

  def validateTsFlow(
                      checkType: String,
                      validationContext: ValidationContext
                    ): Flow[ByteString, HmdaValidated[TransmittalSheet], NotUsed] = {
    val currentYear      = config.getInt("hmda.filing.current")
    val period           = validationContext.filingPeriod.fold(Period(currentYear, None))(identity)
    val validationEngine = selectTsEngine(period.year, period.quarter)
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => TsCsvParser(s))
      .collect {
        case Right(ts) => ts
      }
      .map { ts =>
        val errors = checkType match {
          case "all" =>
            validationEngine.checkAll(ts, ts.LEI, validationContext, TsValidationError)
          case "syntactical" =>
            validationEngine.checkSyntactical(ts, ts.LEI, validationContext, TsValidationError)
          case "validity" =>
            validationEngine.checkValidity(ts, ts.LEI, validationContext, TsValidationError)
          case "quality" =>
            validationEngine.checkQuality(ts, ts.LEI, validationContext)
        }
        (ts, errors)
      }
      .map(x => x._2.leftMap(xs => addTsFieldInformation(x._1, xs.toList, validationContext.institution, period)).toEither)
  }

  def validateTsLarEdits(
                          tsLar: TransmittalLar,
                          checkType: String,
                          validationContext: ValidationContext
                        ): Either[List[ValidationError], TransmittalLar] = {
    val currentYear      = config.getInt("hmda.filing.current")
    val period           = validationContext.filingPeriod.fold(Period(currentYear, None))(identity)
    val validationEngine = selectTsLarEngine(period.year, period.quarter)

    val errors = checkType match {
      case "all" =>
        validationEngine.checkAll(tsLar, tsLar.uli, validationContext, TsValidationError)
      case "syntactical-validity" =>
        validationEngine.checkSyntactical(tsLar, tsLar.uli, validationContext, TsValidationError)
      case "quality" =>
        validationEngine.checkQuality(tsLar, tsLar.uli, validationContext)
    }
    errors.leftMap(xs => addTsFieldInformation(tsLar.ts, xs.toList, Option(Institution.empty), period)).toEither
  }

  def validateLarFlow(checkType: String, ctx: ValidationContext): Flow[ByteString, HmdaValidated[LoanApplicationRegister], NotUsed] = {
    val currentYear      = config.getInt("hmda.filing.current")
    val period           = ctx.filingPeriod.fold(Period(currentYear, None))(identity)
    val validationEngine = selectLarEngine(period.year, period.quarter)
    collectLar.map { lar =>
      def errors =
        checkType match {
          case "all" =>
            validationEngine.checkAll(lar, lar.loan.ULI, ctx, LarValidationError)

          case "syntactical" =>
            validationEngine
              .checkSyntactical(lar, lar.loan.ULI, ctx, LarValidationError)

          case "validity" =>
            validationEngine.checkValidity(lar, lar.loan.ULI, ctx, LarValidationError)

          case "syntactical-validity" =>
            validationEngine
              .checkSyntactical(lar, lar.loan.ULI, ctx, LarValidationError)
              .combine(
                validationEngine.checkValidity(lar, lar.loan.ULI, ctx, LarValidationError)
              )

          case "quality" =>
            validationEngine.checkQuality(lar, lar.loan.ULI, ctx)
        }
      (lar, errors)
    }.map(x => x._2.leftMap(xs => addLarFieldInformation(x._1, xs.toList, period)).toEither)
  }

  def addLarFieldInformation(lar: LoanApplicationRegister, errors: List[ValidationError], period: Period): List[ValidationError] =
    errors.map { error =>
      val affectedFields = EditDescriptionLookup.lookupFields(error.editName, period)
      val fieldMap = ListMap(
        affectedFields.map(field => (field, if (field == "Loan Amount") toBigDecimalString(lar.valueOf(field)) else lar.valueOf(field))): _*
      )
      error.copyWithFields(fieldMap)
    }

  def addTsFieldInformation(
                             ts: TransmittalSheet,
                             errors: List[ValidationError],
                             institution: Option[Institution] = Option(Institution.empty),
                             period: Period
                           ): List[ValidationError] =
    errors.map { error =>
      val affectedFields = EditDescriptionLookup.lookupFields(error.editName, period)
      val fieldMap =
        error.editName match {
          case "S303" =>
            ListMap(
              affectedFields.map(field =>
                (
                  field,
                  "Provided: " + ts.valueOf(field) + ", Expected: " + institution
                    .getOrElse(Institution.empty)
                    .valueOf(field)
                )
              ): _*
            )
          case "Q303" =>
            ListMap(
              affectedFields.map(field =>
                (
                  field,
                  "Provided: " + ts.valueOf(field) + ", Expected: " + institution
                    .getOrElse(Institution.empty)
                    .valueOf(field)
                )
              ): _*
            )
          case "V718" =>
            val quarter =
              period.quarter match {
                case Some("Q1") => "1"
                case Some("Q2") => "2"
                case Some("Q3") => "3"
                case _          => period.quarter.toString
              }
            ListMap(
              affectedFields.map(field =>
                (
                  field,
                  "Provided: " + ts.valueOf(field) + ", Expected: " + quarter
                )
              ): _*
            )
          case _ =>
            ListMap(affectedFields.map(field => (field, ts.valueOf(field))): _*)
        }

      error.copyWithFields(fieldMap)
    }

  def validateAsyncLarFlow(
                            checkType: String,
                            period: Period, validationContext: ValidationContext
                          )(implicit mat: Materializer, ec: ExecutionContext): Flow[ByteString, HmdaValidated[LoanApplicationRegister], NotUsed] = {
    val validationEngine = selectLarEngine(period.year, period.quarter)
    collectLar
      .mapAsync(1) { lar =>
        val futValidation: Future[HmdaValidation[LoanApplicationRegister]] =
          checkType match {
            case "syntactical-validity" =>
              validationEngine.checkValidityAsync(lar, lar.loan.ULI)
            case "quality" =>
              validationEngine.checkQualityAsync(lar, lar.loan.ULI, validationContext)
          }
        futValidation
      }
      .map(hmdaValidationResult => hmdaValidationResult.leftMap(_.toList).toEither)
  }

  private def collectLar =
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => LarCsvParser(s))
      .collect {
        case Right(lar) => lar
      }

}