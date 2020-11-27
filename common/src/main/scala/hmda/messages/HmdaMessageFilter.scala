package hmda.messages

import akka.kafka.ConsumerMessage.{ CommittableMessage, CommittableOffset }
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

object HmdaMessageFilter extends StrictLogging {

  case class StandardMsgKey(lei: String, year: Int, quarter: Option[String], sequenceNumber: Option[String])

  def parse(key: String): Option[StandardMsgKey] = {
    Try {
      // lei1:lei2-year-q1-seq_num
      val regex = "^(?<lei1>[A-Z0-9]+):(?<lei2>[A-Z0-9]+)-(?<year>[0-9]{4})(-(?<quarter>[qQ][1-3]))?(-(?<seqNum>[0-9]+))?$".r
      for {
        onlyMatch <- regex.findFirstMatchIn(key)
        lei1 = onlyMatch.group("lei1")
        lei2 = onlyMatch.group("lei2")
        year = onlyMatch.group("year").toInt
        quarterOpt = Option(onlyMatch.group("quarter"))
        seqNum = Option(onlyMatch.group("seqNum"))
        _ <- if (lei1 == lei2) Some(()) else None
      } yield StandardMsgKey(lei1, year, quarterOpt, seqNum)
    }.toOption.flatten // regex api is not the safest one and we don't want it to throw accidentally
  }

  type Processor = CommittableMessage[String, String] => Future[CommittableOffset]

  def processOnlyValidKeys[V](f: Processor)(implicit ec: ExecutionContext): Processor = msg => {
    parse(msg.record.key()) match {
      case Some(_) => f(msg)
      case None =>
        logger.warn(s"Kafka message has invalid key and will be committed without being processed. Msg: ${msg}")
        Future(msg.committableOffset)
    }
  }
}