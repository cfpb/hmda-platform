package hmda.api.http.public

import akka.NotUsed
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{ Broadcast, Concat, Flow, GraphDSL, Sink, Source }
import akka.stream.{ FlowShape, Materializer }
import akka.util.ByteString
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.validation.ValidationError
import hmda.model.validation.LarValidationError
import hmda.api.http.model.public.LarValidateRequest
import hmda.api.http.model.filing.submissions.HmdaRowParsedErrorSummary
import hmda.api.http.utils.ParserErrorUtils
import hmda.model.validation.LarValidationError
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

import scala.util.{ Failure, Success }

object HmdaFileValidationHttpApi {
  def create(implicit mat: Materializer): Route = new HmdaFileValidationHttpApi().hmdaFileRoutes
}

private class HmdaFileValidationHttpApi(implicit mat: Materializer) {

  //hmda/parse
  private val parseHmdaFileRoute =
    path("parse") {
      fileUpload("file") {
        case (_, byteSource) =>
          val processF =
            byteSource.via(processHmdaFile).runWith(Sink.seq)
          onComplete(processF) {
            case Success(parsed) =>
              val errorList = parsed.filter(_.isDefined)
              complete(errorList)

            case Failure(error) =>
              complete(ToResponseMarshallable(StatusCodes.BadRequest -> error.getLocalizedMessage))
          }
        case _ =>
          complete(ToResponseMarshallable(StatusCodes.BadRequest))
      } ~
        options(complete("OPTIONS"))
    } ~
      path("parse" / "csv") {
        post {
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            fileUpload("file") {
              case (_, byteSource) =>
                val headerSource =
                  Source.fromIterator(() => List("Row Number|Estimated ULI|Field Name|Input Value|Valid Values\n").toIterator)
                val errors = byteSource
                  .via(processHmdaFile)
                  .filter(_.isDefined)
                  .map {
                    case Some(error) => error.toCsv
                    case None        => ""
                  }
                  .map(s => ByteString(s))

                val csvF = headerSource
                  .map(s => ByteString(s))
                  .concat(errors)
                  .map(_.utf8String)
                  .runWith(Sink.seq)

                onComplete(csvF) {
                  case Success(csv) =>
                    complete(ToResponseMarshallable(HttpEntity(ContentTypes.`text/csv(UTF-8)`, csv.mkString("\n"))))
                  case Failure(error) =>
                    complete(ToResponseMarshallable(StatusCodes.BadRequest -> error.getLocalizedMessage))
                }

              case _ =>
                complete(ToResponseMarshallable(StatusCodes.BadRequest))
            }
          }
        } ~
          options(complete("OPTIONS"))
      }

  private val validateYearRoute =
    path("validate" / IntNumber ) { year =>
      parameters('check.as[String] ? "all") { checkType =>
        post {
          respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
            fileUpload("file") {
              case (_, byteSource) =>
                entity(as[LarValidateRequest]) { req =>
                  val processF =
                    byteSource.via(processValidateHmdaFile(year, checkType)).runWith(Sink.seq)
                    onComplete(processF) {
                    case Success(errorList) =>
                      complete(errorList)

                    case Failure(error) =>
                      complete(ToResponseMarshallable(StatusCodes.BadRequest -> error.getLocalizedMessage))
                  }
                }
              case _ =>
                complete(ToResponseMarshallable(StatusCodes.BadRequest))
            }
          }
        }
      }
    }

  def hmdaFileRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          pathPrefix("hmda") {
            parseHmdaFileRoute
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
      case "quality"  => checkQuality(lar, lar.loan.ULI)
    }

    val maybeErrors = validation.leftMap(xs => xs.toList).toEither

    maybeErrors match {
      case Right(l) => None
      case Left(errors) =>
        Some(errors)
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
    }

    val maybeErrors = validation.leftMap(xs => xs.toList).toEither

    maybeErrors match {
      case Right(t) =>
        None

      case Left(errors) =>
        Some(errors)
    }
  }

  private def processHmdaFile: Flow[ByteString, Option[HmdaRowParsedErrorSummary], NotUsed] =
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast  = b.add(Broadcast[ByteString](2))
      val concat = b.add(Concat[Option[HmdaRowParsedErrorSummary]](2))

      bcast ~> processTsSource ~> concat.in(0)
      bcast ~> processLarSource ~> concat.in(1)

      FlowShape(bcast.in, concat.out)
    })

  private def processTsSource: Flow[ByteString, Option[HmdaRowParsedErrorSummary], NotUsed] =
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
        case (i, Right(_)) => None
        case (i, Left(errors)) =>
          Some(ParserErrorUtils.parserValidationErrorSummaryConvertor(i, None, errors))
      }

  private def processLarSource: Flow[ByteString, Option[HmdaRowParsedErrorSummary], NotUsed] =
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
        case (i, lar, Right(_)) => None
        case (i, lar, Left(errors)) =>
          Some(ParserErrorUtils.parserValidationErrorSummaryConvertor(i, Some(lar), errors))
      }

}