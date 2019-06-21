package hmda.parser.filing

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Concat, Flow, GraphDSL}
import akka.util.ByteString
import hmda.model.filing.PipeDelimited
import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.model.filing.ts._2018.TransmittalSheet
import hmda.parser.filing.lar._2018.LarCsvParser
import hmda.parser.filing.ts._2018.TsCsvParser
import hmda.util.streams.FlowUtils.framing

object ParserFlow {

  def parseHmdaFile
    : Flow[ByteString, ParseValidated[PipeDelimited], NotUsed] = {
    Flow.fromGraph(GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      val bcast = b.add(Broadcast[ByteString](2))
      val concat = b.add(Concat[ParseValidated[PipeDelimited]](2))

      bcast.take(1) ~> parseTsFlow ~> concat.in(0)
      bcast.drop(1) ~> parseLarFlow ~> concat.in(1)

      FlowShape(bcast.in, concat.out)
    })
  }

  def parseTsFlow
    : Flow[ByteString, ParseValidated[TransmittalSheet], NotUsed] = {
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(l => TsCsvParser(l))
  }

  def parseLarFlow
    : Flow[ByteString, ParseValidated[LoanApplicationRegister], NotUsed] = {
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(l => LarCsvParser(l))
  }
}
