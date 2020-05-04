package com.hmda.reports

import akka.actor.ActorSystem
import akka.kafka.scaladsl.{ Committer, Consumer }
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ CommitterSettings, ConsumerSettings, Subscriptions }
import akka.pattern.ask
import akka.stream._
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Settings }
import akka.stream.scaladsl._
import akka.util.Timeout
import com.hmda.reports.model.StateMapping
import com.hmda.reports.processing.AggregateProcessing
import com.hmda.reports.processing.AggregateProcessing.ProcessAggregateKafkaRecord
import hmda.messages.pubsub.HmdaTopics
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent._
import scala.concurrent.duration._

object AggregateReports {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession
      .builder()
      .appName("Hmda-Reports-Aggregate")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    implicit val system: ActorSystem  = ActorSystem()
    implicit val mat: Materializer    = Materializer(system)
    implicit val ec: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout     = Timeout(2.hours)

    val JDBC_URL       = sys.env("JDBC_URL").trim()
    val AWS_ACCESS_KEY = sys.env("ACCESS_KEY").trim()
    val AWS_SECRET_KEY = sys.env("SECRET_KEY").trim()
    val AWS_BUCKET     = sys.env("AWS_ENV").trim()

    val awsCredentialsProvider               = StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_ACCESS_KEY, AWS_SECRET_KEY))
    val awsRegionProvider: AwsRegionProvider = () => Region.US_EAST_1

    val s3Settings = S3Settings(system)
      .withBufferType(MemoryBufferType)
      .withCredentialsProvider(awsCredentialsProvider)
      .withS3RegionProvider(awsRegionProvider)
      .withListBucketApiVersion(ListBucketVersion2)
      .withPathStyleAccess(true)

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
        .groupBy(stateMapping => (stateMapping.stateCode, stateMapping.countyCode))
        .mapValues(list => list.head)
    }

    val processorRef = system.actorOf(AggregateProcessing.props(spark, s3Settings), "complex-json-processor")

    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(system.settings.config.getConfig("akka.kafka.consumer"), new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(sys.env("KAFKA_HOSTS"))
        .withGroupId(HmdaTopics.adTopic)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(HmdaTopics.adTopic))
      // async boundary begin
      .async
      .mapAsync(1) { msg =>
        (processorRef ? ProcessAggregateKafkaRecord(
          lookupMap = lookupMap,
          jdbcUrl = JDBC_URL,
          bucket = AWS_BUCKET,
          year = "2018"
        )).map(_ => msg.committableOffset)
      }
      .async
      // async boundary end
      .toMat(Committer.sink(CommitterSettings(system).withMaxBatch(1L)))(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()
  }
}