package com.hmda.reports

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream._
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.scaladsl._
import akka.pattern.ask
import akka.util.Timeout
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.hmda.reports.model.StateMapping
import com.hmda.reports.processing.DisclosureProcessing
import com.hmda.reports.processing.DisclosureProcessing.ProcessDisclosureKafkaRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType
import hmda.messages.pubsub.HmdaTopics

import scala.concurrent.duration._
import scala.concurrent._

object DisclosureReports {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("Hmda-Reports-Disclosure")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout = Timeout(2.hours)

    val JDBC_URL = sys.env("JDBC_URL").trim()
    val AWS_ACCESS_KEY = sys.env("ACCESS_KEY").trim()
    val AWS_SECRET_KEY = sys.env("SECRET_KEY").trim()
    val AWS_BUCKET = sys.env("AWS_ENV").trim()

    val awsCredentialsProvider = new AWSStaticCredentialsProvider(
      new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY))
    val region = "us-east-1"
    val awsRegionProvider = new AwsRegionProvider {
      override def getRegion: String = region
    }
    val s3Settings = S3Settings(
      MemoryBufferType,
      awsCredentialsProvider,
      awsRegionProvider,
      ListBucketVersion2
    ).withPathStyleAccess(true)

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

    val processorRef = system.actorOf(
      DisclosureProcessing.props(spark, s3Settings),
      "complex-json-processor")

    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(system.settings.config.getConfig("akka.kafka.consumer"),
                       new StringDeserializer,
                       new StringDeserializer)
        .withBootstrapServers(sys.env("KAFKA_HOSTS"))
        .withGroupId(HmdaTopics.disclosureTopic)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val drainingControl: DrainingControl[Done] = Consumer
      .committableSource(consumerSettings,
                         Subscriptions.topics(HmdaTopics.disclosureTopic))
      // async boundary begin
      .async
      .mapAsync(1) { msg =>
        (processorRef ? ProcessDisclosureKafkaRecord(lei = msg.record.key,
                                                     lookupMap = lookupMap,
                                                     jdbcUrl = JDBC_URL,
                                                     bucket = AWS_BUCKET,
                                                     year = "2018"))
          .map(_ => msg.committableOffset)
      }
      .async
      // async boundary end
      .mapAsync(1)(offset => offset.commitScaladsl())
      .toMat(Sink.ignore)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()

    val processComplete = Source
      .tick(initialDelay = 26.minutes, interval = 26.minutes, drainingControl)
      .mapAsync(1) { drainingControl =>
        drainingControl.shutdown()
      }
      .take(1)
      .runWith(Sink.ignore)

    processComplete.onComplete { _ =>
      system.terminate()
      spark.stop()
    }
  }
}
