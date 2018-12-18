package hmda.regulator.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.regulator.query.{InstitutionEntity, RegulatorComponent}
import hmda.regulator.scheduler.schedules.Schedules.PanelScheduler

import scala.concurrent.Future

class PanelScheduler extends HmdaActor with RegulatorComponent {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("PanelScheduler", self, PanelScheduler)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler")
  }

  override def receive: Receive = {

    case PanelScheduler =>

      val fileHeader ="lei|activityYear|agency|institutionType|" +
        "id2017|taxId|rssd|respondentName|respondentState|respondentCity|" +
        "parentIdRssd|parentName|assets|otherLenderCode|topHolderIdRssd|topHolderName|hmdaFiler"


      val institutionRepository = new InstitutionRepository(dbConfig)

      val db = institutionRepository.db

      val count = institutionRepository.count()

      val countResults: Future[Int] = institutionRepository.count()
      countResults.foreach(count => { println(s"Filer Found: ($count)") })

      val institutionResults: Future[Seq[InstitutionEntity]] =
        institutionRepository.findActiveFilers()
      institutionResults.foreach(institutions => {
        for (institution <- institutions)
          println(s"Filer Found:" + institution.toPSV)
      })

      val allResults: Future[Seq[InstitutionEntity]] =
        institutionRepository.getAllInstitutions()
      allResults.foreach(institutions => {
        for (institution <- institutions)
          println(s"All Found:" + institution.toPSV)
      })

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
