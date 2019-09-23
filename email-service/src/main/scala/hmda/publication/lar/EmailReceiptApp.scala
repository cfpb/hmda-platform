package hmda.publication.lar

import akka.actor.ActorSystem
import akka.kafka.CommitterSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import hmda.publication.lar.config.Settings
import hmda.publication.lar.email._
import hmda.publication.lar.kafka.Kafka._
import monix.execution.Scheduler

import scala.util.{ Failure, Success }

object EmailReceiptApp {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem    = ActorSystem("email-service")
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val scheduler: Scheduler   = Scheduler(system.dispatcher)

    val log = system.log
    log.info("""
               | _   _ ___  ________  ___    _____                _ _
               || | | ||  \/  |  _  \/ _ \  |  ___|              (_) |
               || |_| || .  . | | | / /_\ \ | |__ _ __ ___   __ _ _| |
               ||  _  || |\/| | | | |  _  | |  __| '_ ` _ \ / _` | | |
               || | | || |  | | |/ /| | | | | |__| | | | | | (_| | | |
               |\_| |_/\_|  |_/___/ \_| |_/ \____/_| |_| |_|\__,_|_|_|
               |
               |""".stripMargin)

    val config         = Settings(system)
    val serviceClient  = AmazonSimpleEmailServiceClientBuilder.defaultClient()
    val emailService   = new SESEmailService(serviceClient, config.email.fromAddress)
    val commitSettings = CommitterSettings(config.kafka.commitSettings)

    val (_, streamCompletion) = pullEmails(system, config.kafka.bootstrapServers, config.kafka.topic, config.kafka.groupId)
      .via(sendEmails(emailService, config.email.content, config.kafka.emailParallelism))
      .toMat(commitMessages(commitSettings))(Keep.both)
      .run()

    streamCompletion.onComplete {
      case Success(value) =>
        log.error(s"Infinite consumer stream is not supposed to completed but it has")
        system.terminate()

      case Failure(exception) =>
        log.error(exception, "Infinite consumer stream failed")
        system.terminate()
    }
  }
}