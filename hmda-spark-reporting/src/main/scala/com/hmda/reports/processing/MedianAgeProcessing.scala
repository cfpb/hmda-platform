package com.hmda.reports.processing

import com.hmda.reports.model._
import com.hmda.reports.processing.BaseProcessing.{includeZeroAndNonZero, prepare}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

object MedianAgeProcessing {

  val ageList = List(
    AgeBuckets("Age Unknown"),
    AgeBuckets("1969 or Earlier"),
    AgeBuckets("1970 - 1979"),
    AgeBuckets("1980 - 1989"),
    AgeBuckets("1990 - 1999"),
    AgeBuckets("2000 - 2010"),
    AgeBuckets("2011 - Present"))
    .toDF


  def prepare(df: DataFrame): DataFrame =
    df.filter(col("msa_md") =!= lit(0))
      .filter(upper(col("tract")) =!= lit("NA"))
      .filter(upper(col("filing_year")) === lit(2018))


  def includeZeroAndNonZero (dispInput: DataFrame, title: String, dispositionName: String,  allUniqueMsaMdTract: DataFrame): DataFrame = {
    val leftAnti = allUniqueMsaMdTract.join(dispInput, dispInput.col("msa_md") === allUniqueMsaMdTract.col("msa_md") and dispInput.col("median_age_calculated") === allUniqueMsaMdTract.col("median_age_calculated"), "left_anti")
    leftAnti
      .withColumn("loan_amount",lit(0.0))
      .withColumn("count", lit(0))
      .union(dispInput)
      .withColumn("dispositionName",lit(dispositionName))
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
      .groupBy(col("msa_md"), col("msa_md_name"), col("state"), col("median_age_calculated"))
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
      .groupBy(col("msa_md"), col("msa_md_name"), col("state"), col("median_age_calculated"))
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
      .groupBy(col("msa_md"), col("msa_md_name"), col("state"), col("median_age_calculated"))
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
      .groupBy(col("msa_md"), col("msa_md_name"), col("state"), col("median_age_calculated"))
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
      .groupBy(col("msa_md"), col("msa_md_name"), col("state"), col("median_age_calculated"))
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
      .groupBy(col("msa_md"), col("msa_md_name"), col("state"), col("median_age_calculated"))
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
      .groupBy(col("msa_md"), col("msa_md_name"), col("state"), col("median_age_calculated"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(
      dispG,
      title,
      "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)",
      allUniqueMsaMdTract)
      .as[Data]
  }


  def allUniqueMsaMdTract(cachedRecordsDf: DataFrame) =
    cachedRecordsDf
      .select(col("msa_md"), col("msa_md_name"), col("state"), col("median_age_calculated"))
      .dropDuplicates()
      .cache()

  def emptyData(msa_md: Long, msa_md_name: String, state: String, dispositionName: String, title: String): List[Data] = {
    val defaultData = Data(msa_md = msa_md, msa_md_name = msa_md_name, state = state, dispositionName = dispositionName, title = title, loan_amount = 0, count = 0)
    val buckets = List("Age Unknown", "1969 or Earlier", "1970 - 1979", "1980 - 1989", "1990 - 1999", "2000 - 2010", "2011 - Present")
    buckets.map(eachBucket => defaultData.copy(median_age_calculated = eachBucket))
  }

  def transformation(outputTable: Dataset[Data]) = {
    outputTable.groupByKey(data => Grouping(data.msa_md, data.msa_md_name, data.state))
      .flatMapGroups{
        case(Grouping(msa_md, msa_md_name, state), datas) =>
          val listData = datas.toList
          val map = emptyData(msa_md, msa_md_name, state, listData.head.dispositionName, listData.head.title).map(d => (d.median_age_calculated, d)).toMap + (listData.head.median_age_calculated -> listData.head)
          map.values ++ listData.tail
      }
  }

}
