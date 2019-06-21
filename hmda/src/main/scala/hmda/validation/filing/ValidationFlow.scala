package hmda.validation.filing

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL}
import akka.util.ByteString
import cats.Semigroup
import hmda.model.filing.EditDescriptionLookup.config
import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.model.filing.{EditDescriptionLookup, PipeDelimited}
import hmda.model.filing.ts._2018.{TransmittalLar, TransmittalSheet}
import hmda.model.validation.{LarValidationError, TsValidationError, ValidationError}
import hmda.parser.filing.lar._2018.LarCsvParser
import hmda.parser.filing.ts._2018.TsCsvParser
import hmda.validation._
import hmda.validation.context.ValidationContext
import hmda.util.streams.FlowUtils._
import hmda.validation.engine._

import scala.collection.immutable._
import scala.concurrent.Future

object ValidationFlow {

  implicit val larSemigroup = new Semigroup[LoanApplicationRegister] {
    override def combine(x: LoanApplicationRegister,
                         y: LoanApplicationRegister): LoanApplicationRegister =
      x
  }

  def validateHmdaFile(checkType: String, ctx: ValidationContext)
    : Flow[ByteString, HmdaValidated[PipeDelimited], NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast = b.add(Broadcast[ByteString](2))
      val concat = b.add(Concat[HmdaValidated[PipeDelimited]](2))

      bcast.take(1) ~> validateTsFlow(checkType, ctx) ~> concat.in(0)
      bcast.drop(1) ~> validateLarFlow(checkType, ctx) ~> concat.in(1)

      FlowShape(bcast.in, concat.out)
    })
  }

  def validateTsFlow(checkType: String, validationContext: ValidationContext)
    : Flow[ByteString, HmdaValidated[TransmittalSheet], NotUsed] = {
    val currentYear = config.getInt("hmda.filing.current")
    val validationEngine = selectTsEngine(
      validationContext.filingYear.getOrElse(currentYear))
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
            validationEngine.checkAll(ts,
                                      ts.LEI,
                                      validationContext,
                                      TsValidationError)
          case "syntactical" =>
            validationEngine.checkSyntactical(ts,
                                              ts.LEI,
                                              validationContext,
                                              TsValidationError)
          case "validity" =>
            validationEngine.checkValidity(ts, ts.LEI, TsValidationError)
        }
        (ts, errors)
      }
      .map { x =>
        x._2
          .leftMap(xs => {
            addTsFieldInformation(x._1, xs.toList)
          })
          .toEither
      }
  }

  def validateTsLarEdits(tsLar: TransmittalLar,
                         checkType: String,
                         validationContext: ValidationContext)
    : Either[List[ValidationError], TransmittalLar] = {
    val currentYear = config.getInt("hmda.filing.current")
    val validationEngine = selectTsLarEngine(
      validationContext.filingYear.getOrElse(currentYear))
    val errors = checkType match {
      case "all" =>
        validationEngine.checkAll(tsLar,
                                  tsLar.ts.LEI,
                                  validationContext,
                                  TsValidationError)
      case "syntactical-validity" =>
        validationEngine.checkSyntactical(tsLar,
                                          tsLar.ts.LEI,
                                          validationContext,
                                          TsValidationError)
      case "quality" =>
        validationEngine.checkQuality(tsLar, tsLar.ts.LEI)
    }
    errors
      .leftMap(xs => {
        addTsFieldInformation(tsLar.ts, xs.toList)
      })
      .toEither
  }

  def validateLarFlow(checkType: String, ctx: ValidationContext)
    : Flow[ByteString, HmdaValidated[LoanApplicationRegister], NotUsed] = {
    val currentYear = config.getInt("hmda.filing.current")
    val validationEngine = selectLarEngine(
      ctx.filingYear.getOrElse(currentYear))
    collectLar
      .map { lar =>
        def errors =
          checkType match {
            case "all" =>
              validationEngine.checkAll(lar,
                                        lar.loan.ULI,
                                        ctx,
                                        LarValidationError)
            case "syntactical" =>
              validationEngine
                .checkSyntactical(lar, lar.loan.ULI, ctx, LarValidationError)
            case "validity" =>
              validationEngine.checkValidity(lar,
                                             lar.loan.ULI,
                                             LarValidationError)
            case "syntactical-validity" =>
              validationEngine
                .checkSyntactical(lar, lar.loan.ULI, ctx, LarValidationError)
                .combine(
                  validationEngine.checkValidity(lar,
                                                 lar.loan.ULI,
                                                 LarValidationError)
                )
            case "quality" => validationEngine.checkQuality(lar, lar.loan.ULI)
          }
        (lar, errors)
      }
      .map { x =>
        x._2
          .leftMap(xs => {
            addLarFieldInformation(x._1, xs.toList)
          })
          .toEither
      }
  }

  def addLarFieldInformation(
      lar: LoanApplicationRegister,
      errors: List[ValidationError]): List[ValidationError] = {
    errors.map(error => {
      val affectedFields = EditDescriptionLookup.lookupFields(error.editName)
      val fieldMap =
        ListMap(affectedFields.map(field => (field, lar.valueOf(field))): _*)
      error.copyWithFields(fieldMap)
    })
  }

  def addTsFieldInformation(
      ts: TransmittalSheet,
      errors: List[ValidationError]): List[ValidationError] = {
    errors.map(error => {
      val affectedFields = EditDescriptionLookup.lookupFields(error.editName)
      val fieldMap =
        ListMap(affectedFields.map(field => (field, ts.valueOf(field))): _*)
      error.copyWithFields(fieldMap)
    })
  }

  def validateAsyncLarFlow[as: AS, mat: MAT, ec: EC](checkType: String,
                                                     year: Int)
    : Flow[ByteString, HmdaValidated[LoanApplicationRegister], NotUsed] = {
    val validationEngine = selectLarEngine(year)
    collectLar
      .mapAsync(1) { lar =>
        val futValidation: Future[HmdaValidation[LoanApplicationRegister]] =
          checkType match {
            case "syntactical-validity" =>
              validationEngine.checkValidityAsync(lar, lar.loan.ULI)
            case "quality" =>
              validationEngine.checkQualityAsync(lar, lar.loan.ULI)
          }
        futValidation.map(validation => (lar, validation))
      }
      .map {
        x: (LoanApplicationRegister, HmdaValidation[LoanApplicationRegister]) =>
          x._2.leftMap(xs => xs.toList).toEither
      }
  }

  private def collectLar = {
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => LarCsvParser(s))
      .collect {
        case Right(lar) => lar
      }
  }

}
