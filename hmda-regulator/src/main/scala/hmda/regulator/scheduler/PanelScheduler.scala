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

  val childActor = context.actorOf(Props[TeacherActor], "teacherActor")

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("PanelScheduler", self, PanelScheduler)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler")
  }

  implicit def akkaSystem = context.system
  val regulatorDataPublisher = {
    akkaSystem.spawn(RegulatorDataPublisher.behavior,
                     RegulatorDataPublisher.name)
  }

  override def receive: Receive = {

    case PanelScheduler =>
      //get filer data and convert it to RegulatorData
      val data = new PanelRegulatorData

      data.`dataType _` = "PANEL"

    //UploadToS3(data)

  }
}
