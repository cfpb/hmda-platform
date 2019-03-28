package com.hmda.reports

import akka.NotUsed
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
import akka.kafka.scaladsl.Consumer.DrainingControl

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

// Usage
sealed trait Element
final case object Data extends Element
final case object NoData extends Element


object DisclosureReports {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher


    println(sys.env("KAFKA_HOSTS"))

    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(system.settings.config.getConfig("akka.kafka.consumer"),
                       new StringDeserializer,
                       new StringDeserializer)
        .withBootstrapServers(sys.env("KAFKA_HOSTS"))
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
      .toMat(Sink.seq)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()
  }

  def processKafkaRecord(
      msg: ConsumerMessage.CommittableMessage[String, String]) = {
    // The Spark DF code would go here
    println(
      s"Received a message - key: ${msg.record.key()}, value: ${msg.record.value()}")
//    Thread.sleep(40)
  }


  def completeIfNoElements[A](duration: FiniteDuration, completionMarker: A): Flow[A, A, NotUsed] =
    Flow[A].idleTimeout(duration).recover {
      case _: TimeoutException => completionMarker
    }

}
