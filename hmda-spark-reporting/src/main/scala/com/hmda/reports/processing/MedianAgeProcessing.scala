package com.hmda.reports.processing

import com.hmda.reports.model._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

object MedianAgeProcessing {

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
      dispInput
        .col("msa_md") === allUniqueMsaMdTract.col("msa_md") and dispInput.col(
        "median_age_calculated") === allUniqueMsaMdTract.col(
        "median_age_calculated"),
      "left_anti"
    )
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
                   spark: SparkSession): Dataset[DataMedAge] = {
    import spark.implicits._
    val dispA = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === 1)
      .filter(col("loan_type") isin (2, 3, 4))
      .groupBy(col("msa_md"), col("msa_md_name"), col("median_age_calculated"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispA,
                          title,
                          "FHA, FSA/RHS & VA (A)",
                          allUniqueMsaMdTract)
      .as[DataMedAge]
  }

  def dispositionB(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[DataMedAge] = {
    import spark.implicits._
    val dispB = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(1))
      .filter(col("loan_type") === lit(1))
      .groupBy(col("msa_md"), col("msa_md_name"), col("median_age_calculated"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispB, title, "Conventional (B)", allUniqueMsaMdTract)
      .as[DataMedAge]
  }

  def dispositionC(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[DataMedAge] = {
    import spark.implicits._
    val dispC = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (31, 32))
      .groupBy(col("msa_md"), col("msa_md_name"), col("median_age_calculated"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispC, title, "Refinancings (C)", allUniqueMsaMdTract)
      .as[DataMedAge]
  }

  def dispositionD(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[DataMedAge] = {
    import spark.implicits._
    val dispD = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(2))
      .groupBy(col("msa_md"), col("msa_md_name"), col("median_age_calculated"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispD,
                          title,
                          "Home Improvement Loans (D)",
                          allUniqueMsaMdTract)
      .as[DataMedAge]
  }

  def dispositionE(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[DataMedAge] = {
    import spark.implicits._
    val dispE = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") =!= lit("1"))
      .filter(col("total_units") =!= lit("2"))
      .filter(col("total_units") =!= lit("3"))
      .filter(col("total_units") =!= lit("4"))
      .groupBy(col("msa_md"), col("msa_md_name"), col("median_age_calculated"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispE,
                          title,
                          "Loans on Dwellings For 5 or More Families (E)",
                          allUniqueMsaMdTract)
      .as[DataMedAge]
  }

  def dispositionF(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[DataMedAge] = {
    import spark.implicits._
    val dispF = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (1, 2, 31, 32))
      .filter(col("loan_type") isin (1, 2, 3, 4)) //should match A - D
      .filter(col("occupancy_type") isin (2, 3))
      .groupBy(col("msa_md"), col("msa_md_name"), col("median_age_calculated"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispF,
                          title,
                          "Nonoccupant Loans from Columns A, B, C ,& D (F)",
                          allUniqueMsaMdTract)
      .as[DataMedAge]
  }

  def dispositionG(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueMsaMdTract: DataFrame,
                   spark: SparkSession): Dataset[DataMedAge] = {
    import spark.implicits._
    val dispG = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (1, 2, 31, 32))
      .filter(col("loan_type") isin (1, 2, 3, 4)) //should match A - D
      .filter(col("construction_method") isin ("2"))
      .groupBy(col("msa_md"), col("msa_md_name"), col("median_age_calculated"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(
      dispG,
      title,
      "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)",
      allUniqueMsaMdTract)
      .as[DataMedAge]
  }

  def allUniqueMsaMdTract(cachedRecordsDf: DataFrame) =
    cachedRecordsDf
      .select(col("msa_md"), col("msa_md_name"), col("median_age_calculated"))
      .dropDuplicates()
      .cache()

  val actionsTakenTable1 = Map(
    "Applications Received" -> List(1, 2, 3, 4, 5),
    "Loans Originated" -> List(1),
    "Applications Approved but not Accepted" -> List(2),
    "Applications Denied by Financial Institution" -> List(3),
    "Applications Withdrawn by Applicant" -> List(4),
    "File Closed for Incompleteness" -> List(5)
  )

  def emptyData(msa_md: Long,
                msa_md_name: String,
                dispositionName: String,
                title: String): List[DataMedAge] = {
    val defaultData = DataMedAge(msa_md = msa_md,
                                 msa_md_name = msa_md_name,
                                 dispositionName = dispositionName,
                                 title = title,
                                 loan_amount = 0,
                                 count = 0)
    val buckets = List("Age Unknown",
                       "1969 or Earlier",
                       "1970 - 1979",
                       "1980 - 1989",
                       "1990 - 1999",
                       "2000 - 2010",
                       "2011 - Present")
    buckets.map(eachBucket =>
      defaultData.copy(median_age_calculated = eachBucket))
  }

  def transformationAddDefaultData(ds: Dataset[DataMedAge],
                                   spark: SparkSession): Dataset[DataMedAge] = {
    import spark.implicits._

    ds.groupByKey(data => (data.msa_md, data.msa_md_name))
      .flatMapGroups {
        case ((msa_md, msa_md_name), elements) =>
          val listData = elements.toList
          val defaultMap = emptyData(msa_md,
                                     msa_md_name,
                                     listData.head.dispositionName,
                                     listData.head.title)
            .map(
              d =>
                (d.median_age_calculated.toUpperCase().trim(),
                 d.title.toUpperCase().trim(),
                 d.dispositionName.toUpperCase().trim()) -> d)
            .toMap
          listData
            .foldLeft(defaultMap) {
              case (acc, next) =>
                acc + ((next.median_age_calculated.toUpperCase().trim(),
                        next.title.toUpperCase().trim(),
                        next.dispositionName.toUpperCase().trim()) -> next)
            }
            .values
      }
  }

  def outputCollectionTable1(cachedRecordsDf: DataFrame,
                             spark: SparkSession): List[DataMedAge] = {

    import spark.implicits._
    
    val outputATable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          transformationAddDefaultData(
            dispositionA(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract(cachedRecordsDf),
                         spark),
            spark)
      }
      .reduce(_ union _)

    val outputBTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          transformationAddDefaultData(
            dispositionB(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract(cachedRecordsDf),
                         spark),
            spark)
      }
      .reduce(_ union _)

    val outputCTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          transformationAddDefaultData(
            dispositionC(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract(cachedRecordsDf),
                         spark),
            spark)
      }
      .reduce(_ union _)

    val outputDTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          transformationAddDefaultData(
            dispositionD(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract(cachedRecordsDf),
                         spark),
            spark)
      }
      .reduce(_ union _)

    val outputETable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          transformationAddDefaultData(
            dispositionE(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract(cachedRecordsDf),
                         spark),
            spark)
      }
      .reduce(_ union _)

    val outputFTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          transformationAddDefaultData(
            dispositionF(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract(cachedRecordsDf),
                         spark),
            spark)
      }
      .reduce(_ union _)

    val outputGTable1 = actionsTakenTable1
      .map {
        case (description, eachList) =>
          transformationAddDefaultData(
            dispositionG(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract(cachedRecordsDf),
                         spark),
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

}
