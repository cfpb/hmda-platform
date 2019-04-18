package com.hmda.reports.processing

import com.hmda.reports.model.{DataRaceEthnicity, Grouping}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._

object RaceGenderProcessing {

  val races = List(
    "Asian",
    "Native Hawaiian or Other Pacific Islander",
    "Free Form Text Only",
    "Race Not Available",
    "American Indian or Alaska Native",
    "Black or African American",
    "2 Or More Minority Races",
    "White",
    "Joint"
  )

  val genders = List("Sex Not Available", "Male", "Female", "Joint")

  val ethnicities = List("Free Form Text Only", "Ethnicity Not Available", "Hispanic or Latino", "Not Hispanic or Latino", "Joint")

  def defaultData(msa_md: Long,
                  msa_md_name: String,
                  state: String,
                  dispositionName: String,
                  title: String): List[DataRaceEthnicity] = {
    def fill(race: String, sex: String, ethnicity: String) = DataRaceEthnicity(
      msa_md = msa_md,
      msa_md_name = msa_md_name,
      state = state,
      dispositionName = dispositionName,
      title = title,
      loan_amount = 0,
      count = 0,
      race = race,
      sex = sex,
      ethnicity = ethnicity
    )
    for {
      gender    <- genders
      race      <- races
      ethnicity <- ethnicities
    } yield fill(race, gender, ethnicity)
  }

  def addDefaultData(ds: Dataset[DataRaceEthnicity]): Dataset[DataRaceEthnicity] =
    ds.groupByKey(data => Grouping(data.msa_md, data.msa_md_name, data.state, data.dispositionName, data.title))
      .flatMapGroups { case (Grouping(msa_md, msa_md_name, state, dispName, title), elements) =>
        val defaultMap = defaultData(msa_md, msa_md_name, state, dispName, title).map(d => (d.race, d.sex, d.ethnicity) -> d).toMap
        elements.foldLeft(defaultMap) { case (acc, next) =>
          acc + ((next.race, next.sex, next.ethnicity) -> next)
        }.values
      }


  def allUniqueCombinations (cachedRecordsDf: DataFrame) =
    cachedRecordsDf
      .select(col("msa_md"), col("msa_md_name"), col("state"), col("race"), col("sex"), col("ethnicity"))
      .dropDuplicates()
      .cache()


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
      dispInput.col("msa_md") === allUniqueMsaMdTract.col("msa_md"),
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
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[DataRaceEthnicity] = {
    import spark.implicits._
    val dispA = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(1))
      .filter(col("loan_type") isin (2, 3, 4))
      .groupBy(col("msa_md"),
        col("msa_md_name"),
        col("state"),
        col("race"),
        col("sex"),
        col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispA, title, "FHA, FSA/RHS & VA (A)", allUniqueCombinations)
      .as[DataRaceEthnicity]
  }

  def dispositionB(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[DataRaceEthnicity] = {
    import spark.implicits._
    val dispB = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(1))
      .filter(col("loan_type") === lit(1))
      .groupBy(col("msa_md"),
        col("msa_md_name"),
        col("state"),
        col("race"),
        col("sex"),
        col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispB, title, "Conventional (B)", allUniqueCombinations)
      .as[DataRaceEthnicity]
  }

  def dispositionC(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[DataRaceEthnicity] = {
    import spark.implicits._
    val dispC = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (31, 32))
      .groupBy(col("msa_md"),
        col("msa_md_name"),
        col("state"),
        col("race"),
        col("sex"),
        col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispC, title, "Refinancings (C)", allUniqueCombinations)
      .as[DataRaceEthnicity]
  }

  def dispositionD(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[DataRaceEthnicity] = {
    import spark.implicits._
    val dispD = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") === lit(2))
      .groupBy(col("msa_md"),
        col("msa_md_name"),
        col("state"),
        col("race"),
        col("sex"),
        col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispD,
      title,
      "Home Improvement Loans (D)",
      allUniqueCombinations)
      .as[DataRaceEthnicity]
  }

  def dispositionE(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[DataRaceEthnicity] = {
    import spark.implicits._
    val dispE = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") =!= lit("1"))
      .filter(col("total_units") =!= lit("2"))
      .filter(col("total_units") =!= lit("3"))
      .filter(col("total_units") =!= lit("4"))
      .groupBy(col("msa_md"),
        col("msa_md_name"),
        col("state"),
        col("race"),
        col("sex"),
        col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispE,
      title,
      "Loans on Dwellings For 5 or More Families (E)",
      allUniqueCombinations)
      .as[DataRaceEthnicity]
  }

  def dispositionF(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[DataRaceEthnicity] = {
    import spark.implicits._
    val dispF = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (1, 2, 31, 32))
      .filter(col("loan_type") isin (1, 2, 3, 4)) //should match A - D
      .filter(col("occupancy_type") isin (2, 3))
      .groupBy(col("msa_md"),
        col("msa_md_name"),
        col("state"),
        col("race"),
        col("sex"),
        col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispF,
      title,
      "Nonoccupant Loans from Columns A, B, C ,& D (F)",
      allUniqueCombinations)
      .as[DataRaceEthnicity]
  }

  def dispositionG(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[DataRaceEthnicity] = {
    import spark.implicits._
    val dispG = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("total_units") isin ("1", "2", "3", "4"))
      .filter(col("loan_purpose") isin (1, 2, 31, 32))
      .filter(col("loan_type") isin (1, 2, 3, 4)) //should match A - D
      .filter(col("construction_method") isin ("2"))
      .groupBy(col("msa_md"),
        col("msa_md_name"),
        col("state"),
        col("race"),
        col("sex"),
        col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(
      dispG,
      title,
      "Loans On Manufactured Home Dwellings From Columns A, B, C & D (G)",
      allUniqueCombinations)
      .as[DataRaceEthnicity]
  }

  def outputCollectionTable3(cachedRecordsDf: DataFrame,
                             spark: SparkSession): List[DataRaceEthnicity] = {
    val actionsTakenTable1 = Map(
      "Applications Received" -> List(1, 2, 3, 4, 5),
      "Loans Originated" -> List(1),
      "Applications Approved but not Accepted" -> List(2),
      "Applications Denied by Financial Institution" -> List(3),
      "Applications Withdrawn by Applicant" -> List(4),
      "File Closed for Incompleteness" -> List(5)
    )

    val outputATable1: Dataset[DataRaceEthnicity] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionA(cachedRecordsDf, description, eachList, allUniqueCombinations(cachedRecordsDf), spark)
            .transform(addDefaultData)
      }
      .reduce(_ union _)

    val outputBTable1: Dataset[DataRaceEthnicity] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionB(cachedRecordsDf, description, eachList, allUniqueCombinations(cachedRecordsDf), spark)
            .transform(addDefaultData)
      }
      .reduce(_ union _)

    val outputCTable1: Dataset[DataRaceEthnicity] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionC(cachedRecordsDf, description, eachList, allUniqueCombinations(cachedRecordsDf), spark)
            .transform(addDefaultData)
      }
      .reduce(_ union _)

    val outputDTable1: Dataset[DataRaceEthnicity] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionD(cachedRecordsDf, description, eachList, allUniqueCombinations(cachedRecordsDf), spark)
            .transform(addDefaultData)
      }
      .reduce(_ union _)

    val outputETable1: Dataset[DataRaceEthnicity] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionE(cachedRecordsDf, description, eachList, allUniqueCombinations(cachedRecordsDf), spark)
            .transform(addDefaultData)
      }
      .reduce(_ union _)

    val outputFTable1: Dataset[DataRaceEthnicity] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionF(cachedRecordsDf, description, eachList, allUniqueCombinations(cachedRecordsDf), spark)
            .transform(addDefaultData)
      }
      .reduce(_ union _)

    val outputGTable1: Dataset[DataRaceEthnicity] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          dispositionG(cachedRecordsDf, description, eachList, allUniqueCombinations(cachedRecordsDf), spark)
            .transform(addDefaultData)
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
