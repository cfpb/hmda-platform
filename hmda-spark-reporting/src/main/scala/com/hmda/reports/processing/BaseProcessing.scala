package com.hmda.reports.processing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.{MultipartUploadResult, S3Attributes, S3Settings}
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.hmda.reports.model._
import hmda.model.census.{Census, State}
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object BaseProcessing {

  def prepare(df: DataFrame): DataFrame =
    df.filter(col("msa_md") =!= lit(0))
      .filter(upper(col("tract")) =!= lit("NA"))
      .filter(upper(col("filing_year")) === lit("2018"))

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
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispA = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === 1)
      .filter(col("loan_type") isin (2, 3, 4))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
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
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispB = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(1))
      .filter(col("loan_type") === lit(1))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispB, title, "Conventional (B)", allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionC(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispC = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (31, 32))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispC, title, "Refinancings (C)", allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionD(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispD = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(2))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
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
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispE = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") =!= lit("1"))
      .filter(col("total_units") =!= lit("2"))
      .filter(col("total_units") =!= lit("3"))
      .filter(col("total_units") =!= lit("4"))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
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
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispF = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (1, 2, 31, 32))
      .filter(col("loan_type") isin (1, 2, 3, 4)) //should match A - D
      .filter(col("occupancy_type") isin (2, 3))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
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
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispG = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (1, 2, 31, 32))
      .filter(col("loan_type") isin (1, 2, 3, 4)) //should match A - D
      .filter(col("construction_method") isin ("2"))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(
      dispG,
      title,
      "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)",
      allUniqueMsaMdTract)
      .as[Data]
  }

  def outputCollectionTable1(cachedRecordsDf: DataFrame,
                             spark: SparkSession): List[Data] = {
    import spark.implicits._
    val actionsTakenTable1 = Map(
      "Applications Received" -> List(1, 2, 3, 4, 5),
      "Loans Originated" -> List(1),
      "Applications Approved but not Accepted" -> List(2),
      "Applications Denied by Financial Institution" -> List(3),
      "Applications Withdrawn by Applicant" -> List(4),
      "File Closed for Incompleteness" -> List(5)
    )

    val outputATable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionA(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputBTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionB(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputCTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionC(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputDTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionD(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputETable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionE(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputFTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionF(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputGTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionG(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val aList = outputATable1.collect().toList
    val bList = outputBTable1.collect().toList
    val cList = outputCTable1.collect().toList
    val dList = outputDTable1.collect().toList
    val eList = outputETable1.collect().toList
    val fList = outputFTable1.collect().toList
    val gList = outputGTable1.collect().toList

    aList ++ bList ++ cList ++ dList ++ eList ++ fList ++ gList
  }

  def outputCollectionTable2(cachedRecordsDf: DataFrame,
                             spark: SparkSession): List[Data] = {
    import spark.implicits._
    val actionsTakenTable2 = Map(
      "Purchased Loans" -> List(6)
    )

    val outputATable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionA(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputBTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionB(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputCTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionC(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputDTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionD(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputETable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionE(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputFTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionF(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputGTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionG(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTract(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val aList = outputATable2.collect().toList
    val bList = outputBTable2.collect().toList
    val cList = outputCTable2.collect().toList
    val dList = outputDTable2.collect().toList
    val eList = outputETable2.collect().toList
    val fList = outputFTable2.collect().toList
    val gList = outputGTable2.collect().toList

    aList ++ bList ++ cList ++ dList ++ eList ++ fList ++ gList
  }

  def allUniqueMsaMdTract(cachedRecordsDf: DataFrame) =
    cachedRecordsDf
      .select(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
      .dropDuplicates()
      .cache()

  def persistSingleFile(fileName: String,
                        data: String,
                        s3Bucket: String,
                        s3Settings: S3Settings)(
      implicit materializer: ActorMaterializer,
      executionContext: ExecutionContext): Future[MultipartUploadResult] =
    Source
      .single(data)
      .map(ByteString(_))
      .runWith(
        S3.multipartUpload(s3Bucket, fileName)
          .withAttributes(S3Attributes.settings(s3Settings))
      )

}
