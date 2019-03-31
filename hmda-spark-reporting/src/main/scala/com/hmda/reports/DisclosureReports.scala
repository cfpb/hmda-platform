package com.hmda.reports

import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream._
import akka.stream.scaladsl._
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

  val JDBC_URL = sys.env("JDBC_URL")
  val KAFKA_HOSTS = sys.env("KAFKA_HOSTS")
  val AWS_ACCESS_KEY = sys.env("ACCESS_KEY")
  val AWS_SECRET_KEY = sys.env("SECRET_KEY")
  val AWS_BUCKET = sys.env("BUCKET")

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher
    val spark: SparkSession = SparkSession
      .builder()
      .appName("Hmda-Reports")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")
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
      .mapAsync(1) { msg =>
        println("Before Running Process Kafka: " + msg)
        DisclosureProcessing
          .processKafkaRecord(msg.record.key,
                              spark,
                              lookupMap,
                              JDBC_URL,
                              KAFKA_HOSTS,
                              AWS_ACCESS_KEY,
                              AWS_SECRET_KEY,
                              "dev",
                              "2018")
          .map(_ => msg.committableOffset)
      }
      .mapAsync(1)(offset => offset.commitScaladsl())
      .toMat(Sink.seq)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()
  }
}
