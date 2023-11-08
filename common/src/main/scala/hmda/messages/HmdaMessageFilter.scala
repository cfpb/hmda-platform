package hmda.messages

import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object HmdaMessageFilter extends StrictLogging {

  case class StandardMsg(lei: String, year: Int, quarter: Option[String], sequenceNumber: Option[String])

  val analyticsConfig: Config = ConfigFactory.load().getConfig("hmda.analytics")

  val yearsAvailable : Seq[String] = analyticsConfig.getString("yearsAvailable").split(",").toSeq



  def parse(key: String, value: String): Option[StandardMsg] = {
    Try {
      val leiRegex = "(?<lei>[A-Z0-9]+)"
      val keyRegex = s"^${leiRegex}$$".r
      // lei1:lei2-year-q1-seq_num
      val msgRegex = s"^${leiRegex}-(?<year>[0-9]{4})(-(?<quarter>[qQ][1-3]))?(-(?<seqNum>[0-9]+))?$$".r
      for {
        keyMatch <- keyRegex.findFirstMatchIn(key)
        msgMatch <- msgRegex.findFirstMatchIn(value)
        lei1 = keyMatch.group("lei")
        lei2 = msgMatch.group("lei")
        year = msgMatch.group("year").toInt
        quarterOpt = Option(msgMatch.group("quarter"))
        seqNum = Option(msgMatch.group("seqNum"))
        _ <- if (lei1 == lei2 && validSeason(year)) Some(()) else None
      } yield StandardMsg(lei1, year, quarterOpt, seqNum)
    }.toOption.flatten // regex api is not the safest one and we don't want it to throw accidentally
  }

  def validSeason(year: Int): Boolean = yearsAvailable.contains(year.toString())

  type Processor = CommittableMessage[String, String] => Future[CommittableOffset]

  def processOnlyValidKeys[V](f: Processor)(implicit ec: ExecutionContext): Processor = msg => {
    parse(msg.record.key(), msg.record.value()) match {
      case Some(_) => f(msg)
      case None =>
        logger.warn(s"Kafka message has invalid key and will be committed without being processed. Msg: ${msg}")
        Future(msg.committableOffset)
    }
  }
}