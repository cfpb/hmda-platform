package hmda.parser.filing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.SubmissionId
import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.util.streams.FlowUtils._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{
  ByteArrayDeserializer,
  StringDeserializer
}

object ParserFlow {

  val config = ConfigFactory.load()

  val kafkaServers = config.getString("kafka.servers")

  val larRawTopic = "lar-raw"

  def apply(submissionId: SubmissionId)(implicit system: ActorSystem): Unit = {
    val consumerSettings =
      ConsumerSettings(system,
                       new ByteArrayDeserializer,
                       new StringDeserializer)
        .withBootstrapServers(kafkaServers)
        .withGroupId("raw-data")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(larRawTopic))
      .map(msg => msg.record)

  }

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
