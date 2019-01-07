package hmda.validation.filing

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL}
import akka.util.ByteString
import cats.Semigroup
import hmda.model.filing.{EditDescriptionLookup, PipeDelimited}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.{TransmittalLar, TransmittalSheet}
import hmda.model.validation.{
  LarValidationError,
  TsValidationError,
  ValidationError
}
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.validation.{AS, EC, HmdaValidated, MAT}
import hmda.validation.context.ValidationContext
import hmda.util.streams.FlowUtils._
import hmda.validation.engine.{LarEngine, TsEngine, TsLarEngine}

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
            TsEngine.checkAll(ts, ts.LEI, validationContext, TsValidationError)
          case "syntactical" =>
            TsEngine.checkSyntactical(ts,
                                      ts.LEI,
                                      validationContext,
                                      TsValidationError)
          case "validity" =>
            TsEngine.checkValidity(ts, ts.LEI, TsValidationError)
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
    val errors = checkType match {
      case "all" =>
        TsLarEngine.checkAll(tsLar,
                             tsLar.ts.LEI,
                             validationContext,
                             TsValidationError)
      case "syntactical" =>
        TsLarEngine.checkSyntactical(tsLar,
                                     tsLar.ts.LEI,
                                     validationContext,
                                     TsValidationError)
      case "validity" =>
        TsLarEngine.checkValidity(tsLar, tsLar.ts.LEI, TsValidationError)
      case "quality" =>
        TsLarEngine.checkQuality(tsLar, tsLar.ts.LEI)
    }
    errors
      .leftMap(xs => {
        addTsFieldInformation(tsLar.ts, xs.toList)
      })
      .toEither
  }

  def validateLarFlow(checkType: String, ctx: ValidationContext)
    : Flow[ByteString, HmdaValidated[LoanApplicationRegister], NotUsed] = {
    val lars = List[LoanApplicationRegister]()
    collectLar
      .map { lar =>
        def errors =
          checkType match {
            case "all" =>
              LarEngine.checkAll(lar, lar.loan.ULI, ctx, LarValidationError)
            case "syntactical" =>
              LarEngine
                .checkSyntactical(lar, lar.loan.ULI, ctx, LarValidationError)
            case "validity" =>
              LarEngine.checkValidity(lar, lar.loan.ULI, LarValidationError)
            case "syntactical-validity" =>
              LarEngine
                .checkSyntactical(lar, lar.loan.ULI, ctx, LarValidationError)
                .combine(
                  LarEngine.checkValidity(lar, lar.loan.ULI, LarValidationError)
                )
            case "quality" => LarEngine.checkQuality(lar, lar.loan.ULI)
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
        affectedFields.map(field => (field, lar.valueOf(field))).toMap
      error.copyWithFields(fieldMap)
    })
  }

  def addTsFieldInformation(
      ts: TransmittalSheet,
      errors: List[ValidationError]): List[ValidationError] = {
    errors.map(error => {
      val affectedFields = EditDescriptionLookup.lookupFields(error.editName)
      val fieldMap =
        affectedFields.map(field => (field, ts.valueOf(field))).toMap
      error.copyWithFields(fieldMap)
    })
  }

  def validateAsyncLarFlow[as: AS, mat: MAT, ec: EC](
      checkType: String): Flow[ByteString,
                               Future[HmdaValidated[LoanApplicationRegister]],
                               NotUsed] = {
    collectLar
      .map { lar =>
        def errors = checkType match {
          case "syntactical-validity" =>
            LarEngine.checkValidityAsync(lar, lar.loan.ULI)
          case "quality" =>
            LarEngine.checkQualityAsync(lar, lar.loan.ULI)
        }
        (lar, errors)
      }
      .map { x =>
        x._2.map(y => y.leftMap(xs => xs.toList).toEither)
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
