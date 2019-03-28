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
import akka.stream._

import scala.concurrent._
import duration._
import akka.stream.scaladsl._
import akka.util.ByteString
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.functions._
import fs2.concurrent.Queue
import monix.eval._
import monix.execution._
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
    implicit val system = ActorSystem()
    implicit val mat = ActorMaterializer()

    val spark: SparkSession = SparkSession
      .builder()
      .appName("Example")
      .config("spark.streaming.receiver.maxRate", 1)
      .config("spark.streaming.blockInterval", 600)
      .config("spark.streaming.backpressure.initialRate", 1)
      .config("spark.streaming.backpressure.enabled", true)
      .config("spark.streaming.kafka.maxRatePerPartition", 1)
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")
    println(spark.read.option("multiline", true).json("/mnt/kafka-config-maps"))

    println(sys.env("JDBC_URL"))
    println(sys.env("ACCESS_KEY"))
    println(sys.env("SECRET_KEY"))
    println(sys.env("KAFKA_HOSTS"))
    val batchInterval = Seconds(10)
    val streamingContext =
      new StreamingContext(spark.sparkContext, batchInterval)
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> sys.env("KAFKA_HOSTS"),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "hmda-spark",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("hmda-spark-reports")

    val kafkaConsumer = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    implicit val taskScheduler: Scheduler = Scheduler.global

    final case class DataReceived()

    val queue = Queue.bounded[Task, DataReceived](1).runSyncUnsafe()

    kafkaConsumer.foreachRDD { rdd =>
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      val localData: Array[(String, String)] =
        rdd.map(cr => (cr.key(), cr.value())).collect()

      if (localData.nonEmpty) {
        queue.offer1(DataReceived()).runSyncUnsafe()
      }
      println("Data Receive: " + localData.size)
      localData.foreach {
        case (key, value) =>
          println(key, value)
          Thread.sleep(40)
      }
      kafkaConsumer.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    }

    val beginKafkaConsumer = Task(kafkaConsumer.start())
    val beginStreaming = Task(streamingContext.start())
    val stopStreaming = Task(
      streamingContext.stop(stopSparkContext = true, stopGracefully = true))
    val waitTermination = Task(streamingContext.awaitTermination()).flatMap(_ =>
      Task.fromFuture(system.terminate()))

    def delayUntilNoDataIsReceived[A](queue: Queue[Task, DataReceived],
                                      exit: Task[A],
                                      delay: FiniteDuration): Task[A] =
      for {
        option <- Task.suspend(queue.tryDequeue1).delayExecution(delay)
        res <- if (option.isEmpty) exit
        else delayUntilNoDataIsReceived(queue, exit, delay)
      } yield res

    val program = for {
      _ <- beginKafkaConsumer
      _ <- beginStreaming
      _ <- delayUntilNoDataIsReceived(queue, stopStreaming, 30.seconds)
      _ <- waitTermination
    } yield ()

    program.runSyncUnsafe()
  }
}
