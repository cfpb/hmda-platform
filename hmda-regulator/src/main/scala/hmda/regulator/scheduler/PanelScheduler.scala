package hmda.regulator.scheduler

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.stream.alpakka.s3.scaladsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.regulator.HmdaRegulatorApp.log
import hmda.regulator.scheduler.schedules.Schedules.PanelScheduler
import hmda.regulator.data.model.PanelRegulatorData
import hmda.regulator.publisher.{RegulatorDataPublisher, UploadToS3}
class PanelScheduler extends HmdaActor with ActorLogging {

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("PanelScheduler", self, PanelScheduler)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler")
  }

  override def receive: Receive = {

    case PanelScheduler =>
    //UploadToS3(data)

  }
}
