package hmda.persistence.processing

import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

import akka.{ Done, NotUsed }
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.{ ActorMaterializer, FlowShape }
import akka.stream.scaladsl.{ Balance, FileIO, Flow, Framing, GraphDSL, Merge, Sink, Source }
import akka.util.ByteString
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.validation.ValidationError
import hmda.parser.fi.lar.LarCsvParser
import hmda.persistence.messages.CommonMessages.Command
//import hmda.persistence.processing.HmdaFileWorker.TestActor.FinishedLarValidation
import hmda.validation.context.ValidationContext
import hmda.validation.engine.LarValidationErrors
import hmda.validation.engine.lar.LarEngine

import scala.concurrent.{ ExecutionContext, Future }
import scalaz.ValidationNel

object HmdaFileWorker extends LarEngine {

  //  implicit val system: ActorSystem = ActorSystem("validation-worker")
  //  implicit val ec: ExecutionContext = system.dispatcher
  //  implicit val mat: ActorMaterializer = ActorMaterializer()

  //  def main(args: Array[String]): Unit = {
  //
  //    val ctx = ValidationContext(None, None)
  //
  //    if (args.length < 1) {
  //      println("Please provide a file")
  //      System.exit(0)
  //    }
  //
  //    val file = new File(args(0))
  //
  //    val replyTo = system.actorOf(TestActor.props)
  //
  //    val source = FileIO.fromPath(file.toPath)
  //
  //    val framing: Flow[ByteString, ByteString, NotUsed] = {
  //      Framing.delimiter(ByteString("\n"), maximumFrameLength = 65536, allowTruncation = true)
  //    }
  //
  //    val larSource: Source[LoanApplicationRegister, Any] =
  //      source
  //        .via(framing)
  //        .drop(1)
  //        .map(_.utf8String)
  //        .map(s => LarCsvParser(s).getOrElse(LoanApplicationRegister()))
  //
  //    println(s"Start Time: ${Instant.now().toString}")
  //    processValidation(larSource, ctx, replyTo, FinishedLarValidation)
  //
  //  }

  def validate(ctx: ValidationContext, replyTo: ActorRef) = {
    Flow[LoanApplicationRegister]
      .map { lar =>
        replyTo ! lar
        validateLar(lar, ctx).toEither
      }
  }
  //
  //  def processValidation(larSource: Source[LoanApplicationRegister, Any], ctx: ValidationContext, replyTo: ActorRef, endMessage: Command): NotUsed = {
  //    val validated: Flow[LoanApplicationRegister, LarValidation, NotUsed] =
  //      Flow[LoanApplicationRegister]
  //        .map { lar =>
  //          replyTo ! lar
  //          validateLar(lar, ctx)
  //        }
  //
  //    larSource
  //      .via(balancer(validated, 3))
  //      .runWith(Sink.actorRef(replyTo, FinishedLarValidation))
  //
  //  }

  def balancer[In, Out](worker: Flow[In, Out, Any], workerCount: Int): Flow[In, Out, NotUsed] = {
    import GraphDSL.Implicits._

    Flow.fromGraph(GraphDSL.create() { implicit b =>
      val balancer = b.add(Balance[In](workerCount, waitForAllDownstreams = true))
      val merge = b.add(Merge[Out](workerCount))

      for (_ <- 1 to workerCount) {
        balancer ~> worker.async ~> merge
      }

      FlowShape(balancer.in, merge.out)
    })
  }

  //  object TestActor {
  //
  //    case object FinishedLarValidation extends Command
  //
  //    def props: Props = Props[TestActor]
  //  }
  //
  //  class TestActor extends Actor {
  //    override def receive: Receive = {
  //      case larValidation: ValidationNel[ValidationError, LoanApplicationRegister] =>
  //
  //      case lar: LoanApplicationRegister =>
  //        println(lar)
  //
  //      case FinishedLarValidation =>
  //        println(s"End Time: ${Instant.now().toString}")
  //
  //    }
  //  }

}
