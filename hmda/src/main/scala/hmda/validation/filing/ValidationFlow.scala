package hmda.validation.filing

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL}
import akka.util.ByteString
import cats.Semigroup
import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.validation.{LarValidationError, TsValidationError}
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.validation.HmdaValidated
import hmda.validation.context.ValidationContext
import hmda.util.streams.FlowUtils._
import hmda.validation.engine.LarEngine
import hmda.validation.engine.TsEngine

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
        checkType match {
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
      }
      .map { x =>
        x.leftMap(xs => xs.toList).toEither
      }
  }

  def validateLarFlow(checkType: String, ctx: ValidationContext)
    : Flow[ByteString, HmdaValidated[LoanApplicationRegister], NotUsed] = {
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(s => LarCsvParser(s))
      .collect {
        case Right(lar) => lar
      }
      .map { lar =>
        checkType match {
          case "all" =>
            LarEngine.checkAll(lar, lar.loan.ULI, ctx, LarValidationError)
          case "syntactical" =>
            LarEngine.checkSyntactical(lar,
                                       lar.loan.ULI,
                                       ctx,
                                       LarValidationError)
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
      }
      .map { x =>
        x.leftMap(xs => xs.toList).toEither
      }
  }
}
