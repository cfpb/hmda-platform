package com.hmda.reports.processing

import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.{MultipartUploadResult, S3Attributes, S3Settings}
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.hmda.reports.model._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

import scala.collection.immutable.ListMap
import scala.concurrent.{ExecutionContext, Future}

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

  def dispositionADisclosure(input: DataFrame,
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
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispA,
                          title,
                          "FHA, FSA/RHS & VA (A)",
                          allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionBDisclosure(input: DataFrame,
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
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispB, title, "Conventional (B)", allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionCDisclosure(input: DataFrame,
                             title: String,
                             actionsTaken: List[Int],
                             allUniqueMsaMdTract: DataFrame,
                             spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispC = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (31, 32))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispC, title, "Refinancings (C)", allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionDDisclosure(input: DataFrame,
                             title: String,
                             actionsTaken: List[Int],
                             allUniqueMsaMdTract: DataFrame,
                             spark: SparkSession): Dataset[Data] = {
    import spark.implicits._
    val dispD = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(2))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispD,
                          title,
                          "Home Improvement Loans (D)",
                          allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionEDisclosure(input: DataFrame,
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
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispE,
                          title,
                          "Loans on Dwellings For 5 or More Families (E)",
                          allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionFDisclosure(input: DataFrame,
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
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispF,
                          title,
                          "Nonoccupant Loans from Columns A, B, C ,& D (F)",
                          allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionGDisclosure(input: DataFrame,
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
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(
      dispG,
      title,
      "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)",
      allUniqueMsaMdTract)
      .as[Data]
  }

  def dispositionA(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[AggregateData] = {
    import spark.implicits._
    val dispA = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === 1)
      .filter(col("loan_type") isin (2, 3, 4))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispA,
                          title,
                          "FHA, FSA/RHS & VA (A)",
                          allUniqueMsaMdTract)
      .as[AggregateData]
  }

  def dispositionB(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[AggregateData] = {
    import spark.implicits._
    val dispB = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(1))
      .filter(col("loan_type") === lit(1))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispB, title, "Conventional (B)", allUniqueMsaMdTract)
      .as[AggregateData]
  }

  def dispositionC(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[AggregateData] = {
    import spark.implicits._
    val dispC = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (31, 32))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispC, title, "Refinancings (C)", allUniqueMsaMdTract)
      .as[AggregateData]
  }

  def dispositionD(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[AggregateData] = {
    import spark.implicits._
    val dispD = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(2))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispD,
                          title,
                          "Home Improvement Loans (D)",
                          allUniqueMsaMdTract)
      .as[AggregateData]
  }

  def dispositionE(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[AggregateData] = {
    import spark.implicits._
    val dispE = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") =!= lit("1"))
      .filter(col("total_units") =!= lit("2"))
      .filter(col("total_units") =!= lit("3"))
      .filter(col("total_units") =!= lit("4"))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispE,
                          title,
                          "Loans on Dwellings For 5 or More Families (E)",
                          allUniqueMsaMdTract)
      .as[AggregateData]
  }

  def dispositionF(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[AggregateData] = {
    import spark.implicits._
    val dispF = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (1, 2, 31, 32))
      .filter(col("loan_type") isin (1, 2, 3, 4)) //should match A - D
      .filter(col("occupancy_type") isin (2, 3))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispF,
                          title,
                          "Nonoccupant Loans from Columns A, B, C ,& D (F)",
                          allUniqueMsaMdTract)
      .as[AggregateData]
  }

  def dispositionG(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[AggregateData] = {
    import spark.implicits._
    val dispG = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (1, 2, 31, 32))
      .filter(col("loan_type") isin (1, 2, 3, 4)) //should match A - D
      .filter(col("construction_method") isin ("2"))
      .groupBy(col("tract"), col("msa_md"), col("msa_md_name"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(
      dispG,
      title,
      "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)",
      allUniqueMsaMdTract)
      .as[AggregateData]
  }

  def outputCollectionTable1(cachedRecordsDf: DataFrame,
                             spark: SparkSession): List[AggregateData] = {
    import spark.implicits._
    val actionsTakenTable1 = ListMap(
      "Loans Originated - (A)" -> List(1),
      "Applications Approved but not Accepted - (B)" -> List(2),
      "Applications Denied by Financial Institution - (C)" -> List(3),
      "Applications Withdrawn by Applicant - (D)" -> List(4),
      "File Closed for Incompleteness - (E)" -> List(5),
      "Applications Received - (F)" -> List(1, 2, 3, 4, 5)
    )

    val outputATable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionA(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputBTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionB(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputCTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionC(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputDTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionD(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputETable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionE(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputFTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionF(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputGTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionG(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
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
                             spark: SparkSession): List[AggregateData] = {
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
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputBTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionB(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputCTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionC(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputDTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionD(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputETable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionE(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputFTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionF(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
                       spark)
      }
      .reduce(_ union _)

    val outputGTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionG(cachedRecordsDf,
                       description,
                       eachList,
                       allUniqueMsaMdTractAggregate(cachedRecordsDf),
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

  def outputCollectionTable1Disclosure(cachedRecordsDf: DataFrame,
                                       spark: SparkSession): List[Data] = {
    import spark.implicits._
    val actionsTakenTable1 = ListMap(
      "Loans Originated - (A)" -> List(1),
      "Applications Approved but not Accepted - (B)" -> List(2),
      "Applications Denied by Financial Institution - (C)" -> List(3),
      "Applications Withdrawn by Applicant - (D)" -> List(4),
      "File Closed for Incompleteness - (E)" -> List(5),
      "Applications Received - (F)" -> List(1, 2, 3, 4, 5)
    )

    val outputATable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionADisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputBTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionBDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputCTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionCDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputDTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionDDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputETable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionEDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputFTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionFDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputGTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionGDisclosure(cachedRecordsDf,
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

  def outputCollectionTable2Disclosure(cachedRecordsDf: DataFrame,
                                       spark: SparkSession): List[Data] = {
    import spark.implicits._
    val actionsTakenTable2 = Map(
      "Purchased Loans" -> List(6)
    )

    val outputATable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionADisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputBTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionBDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputCTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionCDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputDTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionDDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputETable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionEDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputFTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionFDisclosure(cachedRecordsDf,
                                 description,
                                 eachList,
                                 allUniqueMsaMdTract(cachedRecordsDf),
                                 spark)
      }
      .reduce(_ union _)

    val outputGTable2 = actionsTakenTable2
      .map {
        case (description, eachList) =>
          dispositionGDisclosure(cachedRecordsDf,
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
      .select(col("tract"), col("msa_md"), col("msa_md_name"))
      .dropDuplicates()
      .cache()

  def allUniqueMsaMdTractAggregate(cachedRecordsDf: DataFrame) =
    cachedRecordsDf
      .select(col("tract"), col("msa_md"), col("msa_md_name"))
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

  def buildSortedIncomeRace(unsortedRace: IncomeRace): IncomeRace = {
    if (unsortedRace.race == "American Indian or Alaska Native")
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(A)")
    else if (unsortedRace.race == "Asian")
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(B)")
    else if (unsortedRace.race == "Black or African American")
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(C)")
    else if (unsortedRace.race == "Native Hawaiian or Other Pacific Islander")
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(D)")
    else if (unsortedRace.race == "White")
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(E)")
    else if (unsortedRace.race == "2 or more minority races")
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(F)")
    else if (unsortedRace.race == "Joint")
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(H)")
    else if (unsortedRace.race == "Free Form Text Only")
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(I)")
    else
      IncomeRace(unsortedRace.race, unsortedRace.dispositions, "(J)")
  }

  def buildSortedMedAge(unsortedMedAge: MedianAge): MedianAge = {
    if (unsortedMedAge.medianAge == "2011 - Present")
      MedianAge(unsortedMedAge.medianAge, unsortedMedAge.loanCategories, "(A)")
    else if (unsortedMedAge.medianAge == "2000 - 2010")
      MedianAge(unsortedMedAge.medianAge, unsortedMedAge.loanCategories, "(B)")
    else if (unsortedMedAge.medianAge == "1990 - 1999")
      MedianAge(unsortedMedAge.medianAge, unsortedMedAge.loanCategories, "(C)")
    else if (unsortedMedAge.medianAge == "1980 - 1989")
      MedianAge(unsortedMedAge.medianAge, unsortedMedAge.loanCategories, "(D)")
    else if (unsortedMedAge.medianAge == "1970 - 1979")
      MedianAge(unsortedMedAge.medianAge, unsortedMedAge.loanCategories, "(E)")
    else if (unsortedMedAge.medianAge == "1969 or Earlier")
      MedianAge(unsortedMedAge.medianAge, unsortedMedAge.loanCategories, "(F)")
    else
      MedianAge(unsortedMedAge.medianAge, unsortedMedAge.loanCategories, "(G)")
  }

  def buildSortedRace(unsortedRace: Race): Race = {
    if (unsortedRace.race == "American Indian or Alaska Native")
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(A)")
    else if (unsortedRace.race == "Asian")
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(B)")
    else if (unsortedRace.race == "Black or African American")
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(C)")
    else if (unsortedRace.race == "Native Hawaiian or Other Pacific Islander")
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(D)")
    else if (unsortedRace.race == "White")
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(E)")
    else if (unsortedRace.race == "2 or more minority races")
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(F)")
    else if (unsortedRace.race == "Joint")
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(H)")
    else if (unsortedRace.race == "Free Form Text Only")
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(I)")
    else
      Race(unsortedRace.race,
           unsortedRace.dispositions,
           unsortedRace.gender,
           "(J)")
  }

  def buildSortedGender(unsortedGender: Gender): Gender = {
    if (unsortedGender.gender == "Male")
      Gender(unsortedGender.gender, unsortedGender.dispositions, "(A)")
    else if (unsortedGender.gender == "Female")
      Gender(unsortedGender.gender, unsortedGender.dispositions, "(B)")
    else if (unsortedGender.gender == "Joint")
      Gender(unsortedGender.gender, unsortedGender.dispositions, "(C)")
    else
      Gender(unsortedGender.gender, unsortedGender.dispositions, "(D)")
  }

  def buildSortedEthnicity(unsortedEthnicity: Ethnicity): Ethnicity = {
    if (unsortedEthnicity.ethnicityName == "Hispanic or Latino")
      Ethnicity(unsortedEthnicity.ethnicityName,
                unsortedEthnicity.dispositions,
                unsortedEthnicity.gender,
                "(A)")
    else if (unsortedEthnicity.ethnicityName == "Not Hispanic or Latino")
      Ethnicity(unsortedEthnicity.ethnicityName,
                unsortedEthnicity.dispositions,
                unsortedEthnicity.gender,
                "(B)")
    else if (unsortedEthnicity.ethnicityName == "Joint")
      Ethnicity(unsortedEthnicity.ethnicityName,
                unsortedEthnicity.dispositions,
                unsortedEthnicity.gender,
                "(C)")
    else if (unsortedEthnicity.ethnicityName == "Free Form Text Only")
      Ethnicity(unsortedEthnicity.ethnicityName,
                unsortedEthnicity.dispositions,
                unsortedEthnicity.gender,
                "(D)")
    else
      Ethnicity(unsortedEthnicity.ethnicityName,
                unsortedEthnicity.dispositions,
                unsortedEthnicity.gender,
                "(E)")
  }

  def buildSortedApplicantIncome(
      unsortedApplicantIncome: ApplicantIncome): ApplicantIncome = {
    if (unsortedApplicantIncome.applicantIncome == "LESS THAN 50% OF MSA/MD MEDIAN")
      ApplicantIncome(unsortedApplicantIncome.applicantIncome,
                      unsortedApplicantIncome.borrowerCharacteristics,
                      "(A)")
    else if (unsortedApplicantIncome.applicantIncome == "50-79% OF MSA/MD MEDIAN")
      ApplicantIncome(unsortedApplicantIncome.applicantIncome,
                      unsortedApplicantIncome.borrowerCharacteristics,
                      "(B)")
    else if (unsortedApplicantIncome.applicantIncome == "80-99% OF MSA/MD MEDIAN")
      ApplicantIncome(unsortedApplicantIncome.applicantIncome,
                      unsortedApplicantIncome.borrowerCharacteristics,
                      "(C)")
    else if (unsortedApplicantIncome.applicantIncome == "100-119% OF MSA/MD MEDIAN")
      ApplicantIncome(unsortedApplicantIncome.applicantIncome,
                      unsortedApplicantIncome.borrowerCharacteristics,
                      "(D)")
    else
      ApplicantIncome(unsortedApplicantIncome.applicantIncome,
                      unsortedApplicantIncome.borrowerCharacteristics,
                      "(E)")
  }

  def buildSortedIncomeEthnicity(
      unsortedEthnicity: IncomeEthnicity): IncomeEthnicity = {
    if (unsortedEthnicity.ethnicityName == "Hispanic or Latino")
      IncomeEthnicity(unsortedEthnicity.ethnicityName,
                      unsortedEthnicity.dispositions,
                      "(A)")
    else if (unsortedEthnicity.ethnicityName == "Not Hispanic or Latino")
      IncomeEthnicity(unsortedEthnicity.ethnicityName,
                      unsortedEthnicity.dispositions,
                      "(B)")
    else if (unsortedEthnicity.ethnicityName == "Joint")
      IncomeEthnicity(unsortedEthnicity.ethnicityName,
                      unsortedEthnicity.dispositions,
                      "(C)")
    else if (unsortedEthnicity.ethnicityName == "Free Form Text Only")
      IncomeEthnicity(unsortedEthnicity.ethnicityName,
                      unsortedEthnicity.dispositions,
                      "(D)")
    else
      IncomeEthnicity(unsortedEthnicity.ethnicityName,
                      unsortedEthnicity.dispositions,
                      "(E)")
  }

}
