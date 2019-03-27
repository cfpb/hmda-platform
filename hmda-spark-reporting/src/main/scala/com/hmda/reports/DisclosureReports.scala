package com.hmda.reports

import org.apache.spark.sql.expressions._
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.Dataset
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
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.functions._

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

    val spark: SparkSession =
      SparkSession.builder().appName("DisclosureReports").getOrCreate()
    import spark.implicits._
    val df = spark.read
      .format("jdbc")
      .option("driver", "org.postgresql.Driver")
      .option(
        "url",
        ""
      )
      .option(
        "dbtable",
        "(select * from wells_mlar where lei = 'KB1H1DSPRFMYMCUFXT09' and filing_year = 2018) as mlar")
      .load()
      .cache()
    //KB1H1DSPRFMYMCUFXT09
    //B90YWS6AFX2LGWOXJ1LD
    //wells_mlar

    def prepare(df: DataFrame): DataFrame =
      df.filter(col("msa_md") =!= lit(0))
        .filter(upper(col("tract")) =!= lit("NA"))
        .filter(upper(col("filing_year")) === lit(2018))

    def includeZeroAndNonZero(dispInput: DataFrame,
                              title: String,
                              dispositionName: String,
                              allUniqueMsaMdTract: DataFrame): DataFrame = {
      val leftAnti = allUniqueMsaMdTract.join(
        dispInput,
        dispInput.col("tract") === allUniqueMsaMdTract
          .col("tract") and dispInput.col("msa_md") === allUniqueMsaMdTract.col(
          "msa_md"),
        "left_anti")
      leftAnti
        .withColumn("loan_amount", lit(0.0))
        .withColumn("count", lit(0))
        .union(dispInput)
        .withColumn("dispositionName", lit(dispositionName))
        .withColumn("title", lit(title))
    }

    def dispositionA(input: DataFrame,
                     title: String,
                     actionsTaken: List[Int],
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
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
        .groupBy(col("tract"), col("msa_md"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispA,
                            title,
                            "FHA, FSA/RHS & VA (A)",
                            allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionB(input: DataFrame,
                     title: String,
                     actionsTaken: List[Int],
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispB = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin ("1", "2", "3", "4"))
        .filter(col("loan_purpose") === lit(1))
        .filter(col("loan_type") === lit(1))
        .groupBy(col("tract"), col("msa_md"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispB,
                            title,
                            "Conventional (B)",
                            allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionC(input: DataFrame,
                     title: String,
                     actionsTaken: List[Int],
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispC = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin ("1", "2", "3", "4"))
        .filter(col("loan_purpose") isin (31, 32))
        .filter(col("loan_type") === lit(1))
        .groupBy(col("tract"), col("msa_md"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispC,
                            title,
                            "Refinancings (C)",
                            allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionD(input: DataFrame,
                     title: String,
                     actionsTaken: List[Int],
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispD = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin ("1", "2", "3", "4"))
        .filter(col("loan_purpose") === lit(2))
        .filter(col("loan_type") === lit(1))
        .groupBy(col("tract"), col("msa_md"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispD,
                            title,
                            "Home Improvement Loans (D)",
                            allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionE(input: DataFrame,
                     title: String,
                     actionsTaken: List[Int],
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispE = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") =!= lit("1"))
        .filter(col("total_units") =!= lit("2"))
        .filter(col("total_units") =!= lit("3"))
        .filter(col("total_units") =!= lit("4"))
        .groupBy(col("tract"), col("msa_md"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispE,
                            title,
                            "Loans on Dwellings For 5 or More Families (E)",
                            allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionF(input: DataFrame,
                     title: String,
                     actionsTaken: List[Int],
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispF = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin ("1", "2", "3", "4"))
        .filter(col("loan_purpose") isin (1, 2, 31, 32))
        .filter(col("loan_type") === lit(1))
        .filter(col("occupancy_type") isin (2, 3))
        .groupBy(col("tract"), col("msa_md"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(dispF,
                            title,
                            "Nonoccupant Loans from Columns A, B, C ,& D (F)",
                            allUniqueMsaMdTract)
        .as[Data]
    }

    def dispositionG(input: DataFrame,
                     title: String,
                     actionsTaken: List[Int],
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispG = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin ("1", "2", "3", "4"))
        .filter(col("loan_purpose") isin (1, 2, 31, 32))
        .filter(col("loan_type") isin (1, 2, 3, 4))
        .filter(col("occupancy_type") isin (2, 3))
        .groupBy(col("tract"), col("msa_md"))
        .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
      includeZeroAndNonZero(
        dispG,
        title,
        "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)",
        allUniqueMsaMdTract)
        .as[Data]
    }

    val allUniqueMsaMdTract =
      df.select(col("tract"), col("msa_md")).dropDuplicates().cache()

    //Table 1
    //Table 1 Report
    val actionsTakenTable1 = Map(
      "Applications Received" -> List(1, 2, 3, 4, 5),
      "Loans Originated" -> List(1),
      "Applications Approved but not Accepted" -> List(2),
      "Applications Denied by Financial Institution" -> List(3),
      "Applications Withdrawn by Applicant" -> List(4),
      "File Closed for Incompleteness" -> List(5)
    )
    val outputATable1 = (actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionA(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputBTable1 = (actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionB(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputCTable1 = (actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionC(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputDTable1 = (actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionD(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputETable1 = (actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionE(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputFTable1 = (actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionF(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputGTable1 = (actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionG(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputCollectionTable1 = outputATable1.collect().toList ++ outputBTable1
      .collect()
      .toList ++ outputCTable1.collect().toList ++ outputDTable1
      .collect()
      .toList ++ outputETable1.collect().toList ++ outputFTable1
      .collect()
      .toList ++ outputGTable1.collect().toList

    //Table 2

    //Table 2 Report
    val actionsTakenTable2 = Map(
      "Purchased Loans" -> List(6)
    )
    val outputATable2 = (actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionA(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputBTable2 = (actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionB(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputCTable2 = (actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionC(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputDTable2 = (actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionD(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputETable2 = (actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionE(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputFTable2 = (actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionF(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    val outputGTable2 = (actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionG(df, description, eachList, allUniqueMsaMdTract)
      }
      .reduce(_ union _))

    // val outputCollection =  outputA.collect().toList ++ outputB.collect().toList ++ outputC.collect().toList ++ outputD.collect().toList ++ outputE.collect().toList ++ outputF.collect().toList ++ outputG.collect().toList
    val outputCollectionTable2 = outputATable2.collect().toList ++ outputBTable2
      .collect()
      .toList ++ outputCTable2.collect().toList ++ outputDTable2
      .collect()
      .toList ++ outputETable2.collect().toList ++ outputFTable2
      .collect()
      .toList ++ outputGTable2.collect().toList

    def jsonFormationTable1(msaMd: Long, input: List[Data]): Disclosure = {
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
                        val listInfo: List[Info] = datasByTitle.map(d =>
                          Info(d.dispositionName, d.count, d.loan_amount))
                        Disposition(title, listInfo)
                    }
                    .toList

                  Tract(tract, dispositions)
              }
              .toList
            tracts
        }
        .toList

      Disclosure(msaMd, tracts)
    }

    def jsonFormationTable2(msaMd: Long, input: List[Data]): Disclosure2 = {
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
                        val listInfo: List[Info] = datasByTitle.map(d =>
                          Info(d.dispositionName, d.count, d.loan_amount))
                        Disposition(title, listInfo)
                    }
                    .toList

                  Tract2(tract, dispositions(0).values)
              }
              .toList
            tracts
        }
        .toList

      Disclosure2(msaMd, tracts)
    }

    val disclosuresTable1 = outputCollectionTable1.groupBy(d => d.msa_md).map {
      case (key, values) => jsonFormationTable1(key, values)
    }

    val disclosuresTable2 = outputCollectionTable2.groupBy(d => d.msa_md).map {
      case (key, values) => jsonFormationTable2(key, values)
    }

    val awsCredentialsProvider = new AWSStaticCredentialsProvider(
      new BasicAWSCredentials("",
                              ""))
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

    val s3SinkTable1 = s3Client.multipartUpload(
      "cfpb-hmda-public",
      s"dev/reports/disclosure_spark/2018/large/${disclosuresTable1.head.msa}/1.json")

    val s3SinkTable2 = s3Client.multipartUpload(
      "cfpb-hmda-public",
      s"dev/reports/disclosure_spark/2018/large/${disclosuresTable2.head.msa}/2.json")

    val resTable1: Future[MultipartUploadResult] = Source
      .single(disclosuresTable1.head.asJson.noSpaces)
      .map(ByteString(_))
      .toMat(s3SinkTable1)(Keep.right)
      .run()
    Await.ready(resTable1, 30.seconds)
    val resTable2: Future[MultipartUploadResult] = Source
      .single(disclosuresTable2.head.asJson.noSpaces)
      .map(ByteString(_))
      .toMat(s3SinkTable2)(Keep.right)
      .run()
    Await.ready(resTable2, 30.seconds)

    spark.stop()
  }

}
