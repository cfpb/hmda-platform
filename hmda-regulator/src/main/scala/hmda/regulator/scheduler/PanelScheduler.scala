package hmda.regulator.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.actor.Actor.Receive
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.scaladsl.Source
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.query.HmdaQuery.readJournal
import hmda.regulator.HmdaRegulatorApp.log
import hmda.regulator.scheduler.schedules.Schedules.PanelScheduler
import hmda.regulator.data.model.PanelRegulatorData
import hmda.regulator.publisher.{RegulatorDataPublisher, UploadToS3}
import hmda.regulator.query.{InstitutionComponent, InstitutionEntity}
import hmda.regulator.query.InstitutionEntity

class PanelScheduler extends HmdaActor with InstitutionComponent {

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("PanelScheduler", self, PanelScheduler)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler")
  }

  override def receive: Receive = {

    case PanelScheduler =>
      implicit val materializer = ActorMaterializer()

      implicit val institutionRepository = new InstitutionRepository(dbConfig)

      val config = ConfigFactory.load("application.conf").getConfig("aws")

      val accessKeyId = config.getString("access-key-id")
      val secretAccess = config.getString("secret-access-key ")
      val region = config.getString("region")
      val bucket = config.getString("public-bucket")
      val environment = config.getString("environment")
      val year = config.getString("year")

      val awsCredentialsProvider = new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(accessKeyId, secretAccess))

      val awsRegionProvider = new AwsRegionProvider {
        override def getRegion: String = region
      }

      val s3Settings = new S3Settings(
        MemoryBufferType,
        None,
        awsCredentialsProvider,
        awsRegionProvider,
        false,
        None,
        ListBucketVersion2
      )

      val s3Client = new S3Client(s3Settings, context.system, materializer)

      val now = LocalDateTime.now()
      val fileName = s"${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}" + "_PANEL_" + ".txt"

      log.info(s"Uploading Regulator Data file : $fileName" + "  to S3.")

    // val s3Sink = s3Client.multipartUpload(bucket,s"$environment/$bucket/$year/$fileName")
    // val upload:source.filter(i =>  i.hmdaFiler)
//      .via(filterTestBanks(Nil))
//      .map(_.toCSV)
//      .map(_.toString)
//        .map(s => ByteString(s))
//      upload.runWith(s3Sink)
  }
}
