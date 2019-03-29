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
import com.hmda.reports.model.{Data, Info, Disposition, Tract, Tract2, Msa, Disclosure, Disclosure2, OutDisclosure2, OutDisclosure1, Institution, StateMapping}
import org.apache.spark.SparkContext
import org.apache.spark.sql.types.IntegerType

import scala.concurrent.duration._

// Usage
sealed trait Element
final case object Data extends Element
final case object NoData extends Element

object DisclosureReports {

  val JDBC_URL = sys.env("JDBC_URL")
  val KAFKA_HOSTS = sys.env("KAFKA_HOSTS")
  val AWS_ACCESS_KEY = sys.env("KAFKA_HOSTS")
  val AWS_SECRET_KEY = sys.env("KAFKA_HOSTS")

  val spark: SparkSession = SparkSession
    .builder()
    .appName("Hmda-Reports")
    .getOrCreate()
  import spark.implicits._
  spark.sparkContext.setLogLevel("ERROR")

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher





    val consumerSettings: ConsumerSettings[String, String] =
      ConsumerSettings(system.settings.config.getConfig("akka.kafka.consumer"),
                       new StringDeserializer,
                       new StringDeserializer)
        .withBootstrapServers(sys.env("KAFKA_HOSTS"))
        .withGroupId("hmda-spark")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics(""))
      .map { msg =>
        println("came in here")
        processKafkaRecord(msg, spark)
        msg.committableOffset
      }
      .map(offset => offset.commitScaladsl())
      .toMat(Sink.seq)(Keep.both)
      .mapMaterializedValue(DrainingControl.apply)
      .run()
  }

  def completeIfNoElements[A](duration: FiniteDuration, completionMarker: A): Flow[A, A, NotUsed] =
    Flow[A].idleTimeout(duration).recover {
      case _: TimeoutException => completionMarker
    }

  def processKafkaRecord(
      msg: ConsumerMessage.CommittableMessage[String, String], spark: SparkSession) = {
    // The Spark DF code would go here
    println(s"Received a message - key: ${msg.record.key()}, value: ${msg.record.value()}")


    //Return Institution Object

    val leiDetails = spark.read
      .format("jdbc")
      .option("driver", "org.postgresql.Driver")
      .option("url", JDBC_URL)
      .option("dbtable", s"(select lei, institution_name as institutionName from transmittalsheet2018 where lei = '${msg.record.key()}') as transmittalsheet")
      .load().as[Institution].collect().head


    //create lookup map of counties
    val lookupMap: Map[(Int, Int), StateMapping] = {
      spark.read.option("header", "true").csv("s3a://cfpb-hmda-public/dev/reports/disclosure_spark/2018/cbsa_county_name.csv")
        .select(
          col("County/County Equivalent") as "county",
          col("State Name") as "stateName",
          col("FIPS State Code").cast(IntegerType) as "stateCode",
          col("FIPS County Code").cast(IntegerType) as "countyCode")
        .as[StateMapping]
        .collect()
        .toList
        .groupBy(stateMapping => (stateMapping.stateCode, stateMapping.countyCode))
        .mapValues(list => list.head)
    }

    val cachedRecordsDf =
      spark.read
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", JDBC_URL)
        .option("dbtable", s"(select * from modifiedlar2018 where lei = '${msg.record.key()}' and filing_year = 2018) as mlar")
        .load()
        .cache()

    def prepare(df: DataFrame): DataFrame =
      df.filter(col("msa_md") =!= lit(0))
        .filter(upper(col("tract")) =!= lit("NA"))
        .filter(upper(col("filing_year")) === lit(2018))

    def includeZeroAndNonZero(dispInput: DataFrame, title: String, dispositionName: String, allUniqueMsaMdTract: DataFrame): DataFrame = {
      val leftAnti = allUniqueMsaMdTract.join(dispInput, dispInput.col("tract") === allUniqueMsaMdTract.col("tract") and dispInput.col("msa_md") === allUniqueMsaMdTract.col("msa_md"), "left_anti")
      leftAnti
        .withColumn("loan_amount", lit(0.0))
        .withColumn("count", lit(0))
        .union(dispInput)
        .withColumn("dispositionName", lit(dispositionName))
        .withColumn("title", lit(title))
    }

    def dispositionA(input: DataFrame, title: String, actionsTaken: List[Int], allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispA = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(
          (col("total_units") === lit("1")) or
            (col("total_units") === lit("2")) or
            (col("total_units") === lit("3")) or
            (col("total_units") === lit("3")) or
            (col("total_units") === lit("4"))
        )
        .filter(col("loan_purpose") === 1)
        .filter(col("loan_type").isin(1, 2, 3, 4))
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispA, title, "FHA, FSA/RHS & VA (A)", allUniqueMsaMdTract)
        .as[Data]
    }


    def dispositionB(input: DataFrame, title: String, actionsTaken: List[Int], allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispB = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin("1", "2", "3", "4"))
        .filter(col("loan_purpose") === lit(1))
        .filter(col("loan_type") === lit(1))
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispB, title, "Conventional (B)", allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionC(input: DataFrame, title: String, actionsTaken: List[Int], allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispC = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin("1", "2", "3", "4"))
        .filter(col("loan_purpose") isin(31, 32))
        .filter(col("loan_type") === lit(1))
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispC, title, "Refinancings (C)", allUniqueMsaMdTract)
        .as[Data]
    }


    def dispositionD(input: DataFrame, title: String, actionsTaken: List[Int], allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispD = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin("1", "2", "3", "4"))
        .filter(col("loan_purpose") === lit(2))
        .filter(col("loan_type") === lit(1))
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispD, title, "Home Improvement Loans (D)", allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionE(input: DataFrame, title: String, actionsTaken: List[Int], allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispE = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") =!= lit("1"))
        .filter(col("total_units") =!= lit("2"))
        .filter(col("total_units") =!= lit("3"))
        .filter(col("total_units") =!= lit("4"))
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispE, title, "Loans on Dwellings For 5 or More Families (E)", allUniqueMsaMdTract)
        .as[Data]
    }


    def dispositionF(input: DataFrame, title: String, actionsTaken: List[Int], allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispF = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin("1", "2", "3", "4"))
        .filter(col("loan_purpose") isin(1, 2, 31, 32))
        .filter(col("loan_type") === lit(1))
        .filter(col("occupancy_type") isin(2, 3))
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispF, title, "Nonoccupant Loans from Columns A, B, C ,& D (F)", allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionG(input: DataFrame, title: String, actionsTaken: List[Int], allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispG = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin("1", "2", "3", "4"))
        .filter(col("loan_purpose") isin(1, 2, 31, 32))
        .filter(col("loan_type") isin(1, 2, 3, 4))
        .filter(col("occupancy_type") isin(2, 3))
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispG, title, "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)", allUniqueMsaMdTract)
        .as[Data]
    }

    val allUniqueMsaMdTract = cachedRecordsDf.select(col("tract"), col("msa_md"), col("msa_md_name"), col("state")).dropDuplicates().cache()


    val actionsTakenTable1 = Map(
      "Applications Received" -> List(1, 2, 3, 4, 5),
      "Loans Originated" -> List(1),
      "Applications Approved but not Accepted" -> List(2),
      "Applications Denied by Financial Institution" -> List(3),
      "Applications Withdrawn by Applicant" -> List(4),
      "File Closed for Incompleteness" -> List(5)
    )
    val outputATable1 = (actionsTakenTable1.map {
      case (description, eachList) =>
        dispositionA(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))


    val outputBTable1 = (actionsTakenTable1.map {
      case (description, eachList) =>
        dispositionB(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputCTable1 = (actionsTakenTable1.map {
      case (description, eachList) =>
        dispositionC(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputDTable1 = (actionsTakenTable1.map {
      case (description, eachList) =>
        dispositionD(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputETable1 = (actionsTakenTable1.map {
      case (description, eachList) =>
        dispositionE(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputFTable1 = (actionsTakenTable1.map {
      case (description, eachList) =>
        dispositionF(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputGTable1 = (actionsTakenTable1.map {
      case (description, eachList) =>
        dispositionG(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputCollectionTable1 = outputATable1.collect().toList ++ outputBTable1.collect().toList ++ outputCTable1.collect().toList ++ outputDTable1.collect().toList ++ outputETable1.collect().toList ++ outputFTable1.collect().toList ++ outputGTable1.collect().toList


    val actionsTakenTable2 = Map(
      "Purchased Loans" -> List(6)
    )
    val outputATable2 = (actionsTakenTable2.map {
      case (description, eachList) =>
        dispositionA(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputBTable2 = (actionsTakenTable2.map {
      case (description, eachList) =>
        dispositionB(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputCTable2 = (actionsTakenTable2.map {
      case (description, eachList) =>
        dispositionC(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputDTable2 = (actionsTakenTable2.map {
      case (description, eachList) =>
        dispositionD(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputETable2 = (actionsTakenTable2.map {
      case (description, eachList) =>
        dispositionE(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputFTable2 = (actionsTakenTable2.map {
      case (description, eachList) =>
        dispositionF(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputGTable2 = (actionsTakenTable2.map {
      case (description, eachList) =>
        dispositionG(cachedRecordsDf, description, eachList, allUniqueMsaMdTract)
    }.reduce(_ union _))

    val outputCollectionTable2 = outputATable2.collect().toList ++ outputBTable2.collect().toList ++ outputCTable2.collect().toList ++ outputDTable2.collect().toList ++ outputETable2.collect().toList ++ outputFTable2.collect().toList ++ outputGTable2.collect().toList


    def jsonFormationTable1(msaMd: Msa, input: List[Data], leiDetails: Institution): OutDisclosure1 = {
      val tracts = input
        .groupBy(d => d.msa_md)
        .flatMap {
          case (msa, datasByMsa) =>
            val tracts: List[Tract] = datasByMsa
              .groupBy(_.tract)
              .map {
                case (tract, datasByTract) =>
                  val dispositions: List[Disposition] = datasByTract
                    .groupBy(d => d.title)
                    .map {
                      case (title, datasByTitle) =>
                        val listInfo: List[Info] = datasByTitle.map(d => Info(d.dispositionName, d.count, d.loan_amount))
                        Disposition(title, listInfo)
                    }.toList
                  val stateCode = tract.take(2).toInt
                  val countyCode = tract.slice(2, 5).toInt
                  val remainingTract = tract.drop(5)
                  val stateMapping = lookupMap.getOrElse((stateCode, countyCode), StateMapping())
                  Tract(stateMapping.county + "/" + stateMapping.stateName + "/" + remainingTract, dispositions)
              }.toList
            tracts
        }.toList
      OutDisclosure1(leiDetails.lei, leiDetails.institutionName, "1", "Disclosure", "Loans purchased, by location of property and type of loan", 2018, "some date", msaMd, tracts)
    }

    def jsonFormationTable2(msaMd: Msa, input: List[Data], leiDetails: Institution): OutDisclosure2 = {
      val tracts = input
        .groupBy(d => d.msa_md)
        .flatMap {
          case (msa, datasByMsa) =>
            val tracts: List[Tract2] = datasByMsa
              .groupBy(_.tract)
              .map {
                case (tract, datasByTract) =>
                  val dispositions: List[Disposition] = datasByTract
                    .groupBy(d => d.title)
                    .map {
                      case (title, datasByTitle) =>
                        val listInfo: List[Info] = datasByTitle.map(d => Info(d.dispositionName, d.count, d.loan_amount))
                        Disposition(title, listInfo)
                    }.toList
                  val stateCode = tract.take(2).toInt
                  val countyCode = tract.slice(2, 5).toInt
                  val remainingTract = tract.drop(5)
                  val stateMapping = lookupMap.getOrElse((stateCode, countyCode), StateMapping())
                  Tract2(stateMapping.county + "/" + stateMapping.stateName + "/" + remainingTract, dispositions(0).values)
              }.toList
            tracts
        }.toList

      // Disclosure2(msaMd, tracts)
      OutDisclosure2(leiDetails.lei, leiDetails.institutionName, "2", "Disclosure", "Loans purchased, by location of property and type of loan", 2018, "some date", msaMd, tracts)
    }

    val disclosuresTable1 = outputCollectionTable1.groupBy(d => d.msa_md).map {
      case (key, values) =>
        val msaMd = Msa(key.toString(), values.head.msa_md_name, values.head.state, "get_from_census.scala")
        jsonFormationTable1(msaMd, values, leiDetails)
    }

    val disclosuresTable2 = outputCollectionTable2.groupBy(d => d.msa_md).map {
      case (key, values) =>
        val msaMd = Msa(key.toString(), values.head.msa_md_name, values.head.state, "get_from_census.scala")
        jsonFormationTable2(msaMd, values, leiDetails)
    }
  }

}
