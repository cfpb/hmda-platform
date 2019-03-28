package com.hmda.reports

import org.apache.spark.sql.expressions._
import org.apache.spark.sql._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.{MultipartUploadResult, S3Client}
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream._

import scala.concurrent._
import duration._
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.functions._
import fs2.concurrent.Queue
import monix.eval._
import monix.execution._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.concurrent.duration._

case class Data(tract: String,
                msa_md: Long,
                loan_amount: Double,
                count: Long,
                dispositionName: String,
                title: String)
case class Info(dispositionName: String, count: Long, value: Double)
case class Disposition(title: String, values: List[Info])
case class Tract(tract: String, dispositions: List[Disposition])
case class Tract2(tract: String, values: List[Info])
case class Disclosure(msa: Long, tracts: List[Tract])
case class Disclosure2(msa: Long, tracts: List[Tract2])
case class OutDisclosure2(lei: String,
                          institutionName: String,
                          table: String,
                          `type`: String,
                          description: String,
                          year: Int,
                          reportDate: String,
                          msa: Long,
                          tracts: List[Tract2])
case class OutDisclosure1(lei: String,
                          institutionName: String,
                          table: String,
                          `type`: String,
                          description: String,
                          year: Int,
                          reportDate: String,
                          msa: Long,
                          tracts: List[Tract])

object DisclosureReports {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

//    val config = ConfigFactory.load()

    println(sys.env("KAFKA_HOSTS"))

//    val customConf =
//      ConfigFactory.parseString(
//        """
//akka.kaadfadfka.consasdfasumer.polladsf-interval = 50ms
//""")
//
////    val customConf =
////      ConfigFactory.parseString(
////        """
////akka.kafka.consumer.kafka-clients.enable.auto.commit = false
////akka.kafka.consumer.poll-interval = 50ms
////akka.kafka.consumer.poll-timeout = 50ms
////akka.kafka.consumer.stop-timeout = 30s
////akka.kafka.consumer.close-timeout = 20s
////akka.kafka.consumer.commit-timeout = 15s
////akka.kafka.consumer.commit-time-warning = 1s
////akka.kafka.consumer.commit-refresh-interval = infinite
////akka.kafka.consumer.use-dispatcher = "akka.kafka.default-dispatcher"
////akka.kafka.consumer.wait-close-partition = 500ms
////akka.kafka.consumer.position-timeout = 5s
////akka.kafka.consumer.offset-for-times-timeout = 5s
////akka.kafka.consumer.metadata-request-timeout = 5s
////""")
//ConfigFactory.load(customConf)
    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(system.settings.config.getConfig("akka.kafka.consumer"),
                       new StringDeserializer,
                       new StringDeserializer)
        .withBootstrapServers("10.153.98.23:1025")
        .withGroupId("hmda-spark")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings,
                         Subscriptions.topics("hmda-spark-reports"))
      .map { msg =>
        println("came in here")
        processKafkaRecord(msg)
        msg.committableOffset
      }
      .map(offset => offset.commitScaladsl())
  }

  def processKafkaRecord(
      msg: ConsumerMessage.CommittableMessage[String, String]) = {
    // The Spark DF code would go here
    println(
      s"Received a message - key: ${msg.record.key()}, value: ${msg.record.value()}")
//    Thread.sleep(40)
  }
}
