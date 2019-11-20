package com.hmda.reports.processing

import com.hmda.reports.model.{
  DataRaceEthnicity,
  Grouping
}
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
    "2 or more minority races",
    "White",
    "Joint"
  )

  val actionsTaken = Map(
    "Loans Originated - (A)" -> List(1),
    "Applications Approved but not Accepted - (B)" -> List(2),
    "Applications Denied by Financial Institution - (C)" -> List(3),
    "Applications Withdrawn by Applicant - (D)" -> List(4),
    "File Closed for Incompleteness - (E)" -> List(5),
    "Purchased Loans - (F)" -> List(6)
  )

  val genders = List("Sex Not Available", "Male", "Female", "Joint")

  val ethnicities = List("Free Form Text Only",
                         "Ethnicity Not Available",
                         "Hispanic or Latino",
                         "Not Hispanic or Latino",
                         "Joint")

  def defaultData(msa_md: Long,
                  msa_md_name: String): List[DataRaceEthnicity] = {
    def fill(race: String, sex: String, ethnicity: String, title: String) =
      DataRaceEthnicity(
        msa_md = msa_md,
        msa_md_name = msa_md_name,
        title = title,
        loan_amount = 0,
        count = 0,
        race = race,
        sex = sex,
        ethnicity = ethnicity
      )
    for {
      gender <- genders
      race <- races
      ethnicity <- ethnicities
      title <- actionsTaken.keys
    } yield fill(race, gender, ethnicity, title)
  }

  def transformationAddDefaultData(ds: Dataset[DataRaceEthnicity],
                                   spark: SparkSession) = {
    import spark.implicits._
    ds.groupByKey(data => Grouping(data.msa_md, data.msa_md_name))
      .flatMapGroups {
        case (Grouping(msa_md, msa_md_name), elements) =>
          val defaultMap = defaultData(msa_md, msa_md_name)
            .map(d => (d.race, d.sex, d.ethnicity, d.title) -> d)
            .toMap

          elements
            .foldLeft(defaultMap) {
              case (acc, next) =>
                acc + ((next.race, next.sex, next.ethnicity, next.title) -> next)
            }
            .values
      }
  }

  def allUniqueCombinations(cachedRecordsDf: DataFrame) =
    cachedRecordsDf
      .select(col("msa_md"),
              col("msa_md_name"),
              col("race"),
              col("sex"),
              col("ethnicity"))
      .dropDuplicates()
      .cache()

  def prepare(df: DataFrame): DataFrame =
    df.filter(col("msa_md") =!= lit(0))
      .filter(upper(col("tract")) =!= lit("NA"))
      .filter(upper(col("filing_year")) === lit(2018))

  def includeZeroAndNonZero(dispInput: DataFrame,
                            title: String,
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
      .withColumn("title", lit(title))
  }

  def disposition(input: DataFrame,
                  title: String,
                  actionsTaken: List[Int],
                  allUniqueCombinations: DataFrame,
                  spark: SparkSession): Dataset[DataRaceEthnicity] = {
    import spark.implicits._
    val disp = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .groupBy(col("msa_md"),
               col("msa_md_name"),
               col("race"),
               col("sex"),
               col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(disp, title, allUniqueCombinations)
      .as[DataRaceEthnicity]
  }

  def outputCollectionTable3and4(
      cachedRecordsDf: DataFrame,
      spark: SparkSession): List[DataRaceEthnicity] = {

    val output: Dataset[DataRaceEthnicity] = actionsTaken
      .map {
        case (description, eachList) =>
          transformationAddDefaultData(
            disposition(cachedRecordsDf,
                        description,
                        eachList,
                        allUniqueCombinations(cachedRecordsDf),
                        spark),
            spark)
      }
      .reduce(_ union _)

    output.collect().toList
  }

}
