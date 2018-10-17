package hmda.parser.filing

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.util.streams.FlowUtils._

object ParserFlow {

  val config = ConfigFactory.load()

  val kafkaServers = config.getString("kafka.servers")

  val larRawTopic = "lar-raw"

  def parseTsFlow: Flow[ByteString,
                        Either[List[ParserValidationError], TransmittalSheet],
                        NotUsed] = {
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(l => TsCsvParser(l))
  }

  def parseLarFlow
    : Flow[ByteString,
           Either[List[ParserValidationError], LoanApplicationRegister],
           NotUsed] = {
    Flow[ByteString]
      .via(framing("\n"))
      .map(_.utf8String)
      .map(_.trim)
      .map(l => LarCsvParser(l))
  }

}
