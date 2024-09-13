package hmda.api.http.public

import akka.NotUsed
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{ Broadcast, Concat, Flow, GraphDSL, Sink, Source }
import akka.stream.{ FlowShape, Materializer }
import akka.util.ByteString
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.validation.ValidationError
import hmda.model.validation.LarValidationError
import hmda.api.http.model.filing.submissions.HmdaRowParsedErrorSummary
import hmda.api.http.model.filing.submissions.{ ValidationErrorSummary, SingleValidationErrorSummary }
import hmda.api.http.utils.ParserErrorUtils
import hmda.model.validation.TsValidationError
import hmda.validation.engine._
import hmda.validation.context.ValidationContext
import hmda.utils.YearUtils.Period
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.HmdaValidation
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.util.streams.FlowUtils._
import io.circe.generic.auto._
import hmda.model.filing.EditDescriptionLookup._
import scala.collection.immutable._
import hmda.validation.filing.ValidationFlow._

import scala.util.{ Failure, Success }
// $COVERAGE-OFF$
object HmdaFileValidationHttpApi {
  def create(implicit mat: Materializer): Route = new HmdaFileValidationHttpApi().hmdaValidationFileRoutes
}

private class HmdaFileValidationHttpApi(implicit mat: Materializer) {

  private val validateYearRoute =
    path("validate" / IntNumber ) { year =>
      post {
        parameters('check.as[String] ? "all") { checkType =>
          fileUpload("file") {
            case (_, byteSource) =>
              val processF =
                byteSource.via(processValidateHmdaFile(year.toInt, checkType)).runWith(Sink.seq)
              onComplete(processF) {
                case Success(errorList) =>
                  val validationSummary = splitEitherList(errorList, Period(year, None))
                  complete(validationSummary)

                case Failure(error) =>
                  complete(ToResponseMarshallable(StatusCodes.BadRequest -> error.getLocalizedMessage))
              }
            case _ =>
              complete(ToResponseMarshallable(StatusCodes.BadRequest))
          }
        }
      }
    }

  def hmdaValidationFileRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("hmda") {
            validateYearRoute
          }
        }
      }
    }

  private def processValidateHmdaFile(year: Int, checkType: String): Flow[ByteString, Either[HmdaRowParsedErrorSummary, Option[List[ValidationError]]], NotUsed] =
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast  = b.add(Broadcast[ByteString](2))
      val concat = b.add(Concat[Either[HmdaRowParsedErrorSummary, Option[List[ValidationError]]]](2))

      bcast ~> processValidateTsSource(year: Int, checkType: String) ~> concat.in(0)
      bcast ~> processValidateLarSource(year: Int, checkType: String) ~> concat.in(1)

      FlowShape(bcast.in, concat.out)
    })

  private def processValidateTsSource(year: Int, checkType: String): Flow[ByteString, Either[HmdaRowParsedErrorSummary, Option[List[ValidationError]]], NotUsed] =
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .take(1)
      .zip(Source.fromIterator(() => Iterator.from(1)))
      .map {
        case (ts, index) =>
          (index, TsCsvParser(ts))
      }
      .map {
        case (i, Right(ts)) => Right(validateTs(ts, checkType, year, None))
        case (i, Left(errors)) =>
          Left(ParserErrorUtils.parserValidationErrorSummaryConvertor(i, None, errors))
      }

  private def processValidateLarSource(year: Int, checkType: String): Flow[ByteString, Either[HmdaRowParsedErrorSummary, Option[List[ValidationError]]], NotUsed] =
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .drop(1)
      .map(_.trim)
      .zip(Source.fromIterator(() => Iterator.from(2)))
      .map {
        case (lar, index) =>
          (index, lar, LarCsvParser(lar))
      }
      .map {
        case (i, larString, Right(lar)) => Right(validateLar(lar, checkType, year, None))
        case (i, lar, Left(errors)) =>
          Left(ParserErrorUtils.parserValidationErrorSummaryConvertor(i, Some(lar), errors))
      }

  private def validateLar(lar: LoanApplicationRegister, checkType: String, year: Int, quarter: Option[String]): Option[List[ValidationError]] = {
    val ctx              = ValidationContext(filingPeriod = Some(Period(year, quarter)))
    val validationEngine = selectLarEngine(year, None)
    import validationEngine._
    val validation: HmdaValidation[LoanApplicationRegister] = checkType match {
      case "all" => checkAll(lar, lar.loan.ULI, ctx, LarValidationError)
      case "syntactical" =>
        checkSyntactical(lar, lar.loan.ULI, ctx, LarValidationError)
      case "validity" => checkValidity(lar, lar.loan.ULI, ctx, LarValidationError)
      case "quality"  => checkQuality(lar, lar.loan.ULI, ctx)
    }

    val maybeErrors = validation.leftMap(xs => xs.toList).toEither

    maybeErrors match {
      case Right(l) => None
      case Left(errors) =>
        Some(addLarFieldInformation(lar, errors, Period(year, quarter)))
    }
  }

  private def validateTs(ts: TransmittalSheet, checkType: String, year: Int, quarter: Option[String]): Option[List[ValidationError]] = {
    val ctx              = ValidationContext(filingPeriod = Some(Period(year, quarter)))
    val period           = ctx.filingPeriod.get
    val validationEngine = selectTsEngine(period.year, period.quarter)
    import validationEngine._
    val validation: HmdaValidation[TransmittalSheet] = checkType match {
      case "all" =>
        checkAll(ts, ts.LEI, ctx, TsValidationError)

      case "syntactical" =>
        checkSyntactical(ts, ts.LEI, ctx, TsValidationError)

      case "validity" =>
        checkValidity(ts, ts.LEI, ctx, TsValidationError)
      
      case "quality" =>
        checkQuality(ts, ts.LEI, ctx)
    }

    val maybeErrors = validation.leftMap(xs => xs.toList).toEither

    maybeErrors match {
      case Right(t) =>
        None

      case Left(errors) =>
        Some(addTsFieldInformation(ts, errors, None, Period(year, quarter)))
    }
  }

  def splitEitherList(el: Seq[Either[HmdaRowParsedErrorSummary, Option[List[ValidationError]]]], period: Period): ValidationErrorSummary = {
    val (lefts, rights) = el.partition(_.isLeft)
    ValidationErrorSummary(
      lefts.map(_.left.get),
      rights.map(_.right.get).filter(_.isDefined).map(_.get).map(_.map(validationErrorToSummary(_, period)))
    )
  }

  def validationErrorToSummary(ve: ValidationError, period: Period): SingleValidationErrorSummary = {
    SingleValidationErrorSummary(ve.uli, ve.editName, lookupDescription(ve.editName, period), ve.fields)
  }

}
// $COVERAGE-ON$