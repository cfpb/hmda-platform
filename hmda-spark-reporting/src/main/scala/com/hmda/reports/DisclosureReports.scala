package com.hmda.reports

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream._
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3Client
import akka.stream.scaladsl._
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.hmda.reports.model.StateMapping
import com.hmda.reports.processing.DisclosureProcessing
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType
import hmda.messages.pubsub.HmdaTopics.disclosureTopic

import scala.concurrent._

object DisclosureReports {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("Hmda-Reports")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val JDBC_URL = sys.env("JDBC_URL")
    val KAFKA_HOSTS = sys.env("KAFKA_HOSTS")
    val AWS_ACCESS_KEY = sys.env("ACCESS_KEY")
    val AWS_SECRET_KEY = sys.env("SECRET_KEY")
    //  val AWS_BUCKET = sys.env("BUCKET")
    val AWS_BUCKET = "dev"

    val awsCredentialsProvider = new AWSStaticCredentialsProvider(
      new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY))
    val region = "us-east-1"
    val awsRegionProvider = new AwsRegionProvider {
      override def getRegion: String = region
    }
    val s3Settings = S3Settings(
      MemoryBufferType,
      None,
      awsCredentialsProvider,
      awsRegionProvider,
      pathStyleAccess = true,
      None,
      ListBucketVersion2
    )

    val s3Client: S3Client = new S3Client(s3Settings)

    import spark.implicits._
    //    create lookup map of counties
    val lookupMap: Map[(Int, Int), StateMapping] = {
      spark.read
        .option("header", "true")
        .csv("s3a://cfpb-hmda-public/dev/cbsa_county_name.csv")
        .select(
          col("County/County Equivalent") as "county",
          col("State Name") as "stateName",
          col("FIPS State Code").cast(IntegerType) as "stateCode",
          col("FIPS County Code").cast(IntegerType) as "countyCode"
        )
        .as[StateMapping]
        .collect()
        .toList
        .groupBy(stateMapping =>
          (stateMapping.stateCode, stateMapping.countyCode))
        .mapValues(list => list.head)
    }

    println("CAME HERE!!!!")

    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(system.settings.config.getConfig("akka.kafka.consumer"),
                       new StringDeserializer,
                       new StringDeserializer)
        .withBootstrapServers(sys.env("KAFKA_HOSTS"))
        .withGroupId("hmda-spark")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings,
                         Subscriptions.topics(disclosureTopic))
      // async boundary begin
      .async
      .mapAsync(1) { msg =>
        println("About to process: " + msg.record.key)
        DisclosureProcessing
          .processKafkaRecord(msg.record.key,
                              spark,
                              lookupMap,
                              JDBC_URL,
                              "dev",
                              "2018",
                              s3Client)
          .map(_ => msg.committableOffset)
      }
      .async
      // async boundary end
      .mapAsync(1)(offset => offset.commitScaladsl())
      .toMat(Sink.seq)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()
  }
}
