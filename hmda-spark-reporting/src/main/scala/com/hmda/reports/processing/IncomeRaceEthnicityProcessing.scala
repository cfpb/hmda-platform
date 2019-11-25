package com.hmda.reports.processing

import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions._
import com.hmda.reports.model._

object IncomeRaceEthnicityProcessing {
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

  val ethnicities = List(
    "Free Form Text Only",
    "Ethnicity Not Available",
    "Hispanic or Latino",
    "Not Hispanic or Latino",
    "Joint"
  )

  def defaultData(msa_md: Long,
                  msa_md_name: String,
                  incomeBracket: String,
                  title: String): List[IncomeData] = {
    def fill(race: String, ethnicity: String) = {
      IncomeData(
        msa_md = msa_md,
        msa_md_name = msa_md_name,
        incomeBracket = incomeBracket,
        title = title,
        loan_amount = 0,
        count = 0,
        race = race,
        ethnicity = ethnicity
      )
    }
    for {
      race <- races
      ethnicity <- ethnicities
    } yield fill(race, ethnicity)
  }

  def addDefaultData(ds: Dataset[IncomeData], spark: SparkSession) = {
    import spark.implicits._
    ds.groupByKey(
        data =>
          IncomeGrouping(data.msa_md,
                         data.msa_md_name,
                         data.incomeBracket,
                         data.title))
      .flatMapGroups {
        case (IncomeGrouping(msa_md, msa_md_name, dispName, title), elements) =>
          val defaultMap =
            defaultData(msa_md, msa_md_name, dispName, title)
              .map(d => (d.race, d.ethnicity) -> d)
              .toMap
          elements
            .foldLeft(defaultMap) {
              case (acc, next) =>
                acc + ((next.race, next.ethnicity) -> next)
            }
            .values
      }
  }

  def allUniqueCombinations(cachedRecordsDf: DataFrame) =
    cachedRecordsDf
      .select(col("msa_md"), col("msa_md_name"), col("race"), col("ethnicity"))
      .dropDuplicates()
      .cache()

  def prepare(df: DataFrame): DataFrame =
    df.filter(col("msa_md") =!= lit(0))
      .filter(upper(col("tract")) =!= lit("NA"))
      .filter(upper(col("filing_year")) === lit(2018))

  def includeZeroAndNonZero(dispInput: DataFrame,
                            title: String,
                            incomeBracket: String,
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
      .withColumn("incomeBracket", lit(incomeBracket))
      .withColumn("title", lit(title))
  }

  def buildDisposition(input: List[IncomeData],
                       dispositionName: String): IncomeDisposition =
    input.foldLeft(IncomeDisposition(dispositionName, 0, 0, dispositionName)) {
      case (IncomeDisposition(name, curCount, curValue, nameForSorting),
            next) =>
        IncomeDisposition(name.split("-")(0).trim,
                          curCount + next.count,
                          curValue + next.loan_amount,
                          nameForSorting)
    }

  def dispositionA(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[IncomeData] = {
    import spark.implicits._
    val dispA = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("percent_median_msa_income") === "<50%")
      .groupBy(col("msa_md"), col("msa_md_name"), col("race"), col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispA,
                          title,
                          "LESS THAN 50% OF MSA/MD MEDIAN",
                          allUniqueCombinations)
      .as[IncomeData]
  }

  def dispositionB(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[IncomeData] = {
    import spark.implicits._
    val dispB = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("percent_median_msa_income") === "50-79%")
      .groupBy(col("msa_md"), col("msa_md_name"), col("race"), col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispB,
                          title,
                          "50-79% OF MSA/MD MEDIAN",
                          allUniqueCombinations)
      .as[IncomeData]
  }

  def dispositionC(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[IncomeData] = {
    import spark.implicits._
    val dispC = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("percent_median_msa_income") === "80-99%")
      .groupBy(col("msa_md"), col("msa_md_name"), col("race"), col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispC,
                          title,
                          "80-99% OF MSA/MD MEDIAN",
                          allUniqueCombinations)
      .as[IncomeData]
  }

  def dispositionD(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[IncomeData] = {
    import spark.implicits._
    val dispD = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("percent_median_msa_income") === "100-119%")
      .groupBy(col("msa_md"), col("msa_md_name"), col("race"), col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispD,
                          title,
                          "100-119% OF MSA/MD MEDIAN",
                          allUniqueCombinations)
      .as[IncomeData]
  }

  def dispositionE(input: DataFrame,
                   title: String,
                   actionsTaken: List[Int],
                   allUniqueCombinations: DataFrame,
                   spark: SparkSession): Dataset[IncomeData] = {
    import spark.implicits._
    val dispE = prepare(input)
      .filter(col("action_taken_type").isin(actionsTaken: _*))
      .filter(col("percent_median_msa_income") === ">120%")
      .groupBy(col("msa_md"), col("msa_md_name"), col("race"), col("ethnicity"))
      .agg(sum("loan_amount") as "loan_amount", count("*") as "count")
    includeZeroAndNonZero(dispE,
                          title,
                          "120% OR MORE OF MSA/MD MEDIAN",
                          allUniqueCombinations)
      .as[IncomeData]
  }

  def outputCollectionTableIncome(cachedRecordsDf: DataFrame,
                                  spark: SparkSession): List[IncomeData] = {
    val actionsTakenTable1 = Map(
      "Applications Received - (A)" -> List(1, 2, 3, 4, 5),
      "Loans Originated - (B)" -> List(1),
      "Applications Approved but not Accepted - (C)" -> List(2),
      "Applications Denied by Financial Institution - (D)" -> List(3),
      "Applications Withdrawn by Applicant - (E)" -> List(4),
      "File Closed for Incompleteness - (F)" -> List(5),
      "Purchased Loans - (G)" -> List(6)
    )

    val outputATable1: Dataset[IncomeData] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          addDefaultData(dispositionA(cachedRecordsDf,
                                      description,
                                      eachList,
                                      allUniqueCombinations(cachedRecordsDf),
                                      spark),
                         spark)
      }
      .reduce(_ union _)

    val outputBTable1: Dataset[IncomeData] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          addDefaultData(dispositionB(cachedRecordsDf,
                                      description,
                                      eachList,
                                      allUniqueCombinations(cachedRecordsDf),
                                      spark),
                         spark)
      }
      .reduce(_ union _)

    val outputCTable1: Dataset[IncomeData] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          addDefaultData(dispositionC(cachedRecordsDf,
                                      description,
                                      eachList,
                                      allUniqueCombinations(cachedRecordsDf),
                                      spark),
                         spark)
      }
      .reduce(_ union _)

    val outputDTable1: Dataset[IncomeData] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          addDefaultData(dispositionD(cachedRecordsDf,
                                      description,
                                      eachList,
                                      allUniqueCombinations(cachedRecordsDf),
                                      spark),
                         spark)
      }
      .reduce(_ union _)

    val outputETable1: Dataset[IncomeData] = actionsTakenTable1
      .map {
        case (description, eachList) =>
          addDefaultData(dispositionE(cachedRecordsDf,
                                      description,
                                      eachList,
                                      allUniqueCombinations(cachedRecordsDf),
                                      spark),
                         spark)
      }
      .reduce(_ union _)

    val aList = outputATable1.collect().toList
    val bList = outputBTable1.collect().toList
    val cList = outputCTable1.collect().toList
    val dList = outputDTable1.collect().toList
    val eList = outputETable1.collect().toList

    aList ++ bList ++ cList ++ dList ++ eList
  }

  def jsonFormationApplicantIncome(
      input: List[IncomeData]): List[ReportByApplicantIncome] = {
    val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")

    input
      .filter(data => (data.race != null && data.ethnicity != null))
      .groupBy(data => (data.msa_md, data.msa_md_name))
      .map {
        case ((msa_md, msa_md_name), dataForMsa: List[IncomeData]) => {
          val totalGrouping: List[ApplicantIncome] = {
            dataForMsa
              .groupBy(_.incomeBracket)
              .map {
                case (eachIncome, dataForIncome: List[IncomeData]) => {
                  val borrowerRace: BorrowerRace = {
                    val races: List[IncomeRace] =
                      dataForIncome
                        .groupBy(_.race)
                        .map {
                          case (eachRace: String,
                                dataForRace: List[IncomeData]) =>
                            val dispositions = dataForRace
                              .groupBy(_.title)
                              .map {
                                case (eachDisposition: String,
                                      dataForDisposition: List[IncomeData]) => {
                                  buildDisposition(dataForDisposition,
                                                   eachDisposition)
                                }
                              }
                              .toList
                              .sorted
                            BaseProcessing.buildSortedIncomeRace(
                              IncomeRace(eachRace, dispositions, "unsorted"))
                        }
                        .toList
                        .sorted
                    BorrowerRace("Race", races)
                  }

                  val borrowerEthnicity: BorrowerEthnicity = {
                    val ethnicities: List[IncomeEthnicity] = {
                      dataForIncome
                        .groupBy(_.ethnicity)
                        .map {
                          case (eachEthnicity: String,
                                dataForEthnicity: List[IncomeData]) =>
                            val dispositions = dataForEthnicity
                              .groupBy(_.title)
                              .map {
                                case (eachDisposition: String,
                                      dataForDisposition: List[IncomeData]) => {
                                  buildDisposition(dataForDisposition,
                                                   eachDisposition)
                                }
                              }
                              .toList
                              .sorted
                            BaseProcessing.buildSortedIncomeEthnicity(
                              IncomeEthnicity(eachEthnicity,
                                              dispositions,
                                              "unsorted"))
                        }
                        .toList
                        .sorted
                    }
                    BorrowerEthnicity("Ethnicity", ethnicities)
                  }

                  BaseProcessing.buildSortedApplicantIncome(
                    ApplicantIncome(eachIncome,
                                    BorrowerCharacteristics(borrowerRace,
                                                            borrowerEthnicity),
                                    "unsorted"))
                }
              }
              .toList
              .sorted

          }
          val msa =
            Msa(msa_md.toString(), msa_md_name, "", "")
          ReportByApplicantIncome(
            "5",
            "Aggregate",
            "Disposition of applications by income, race, and ethnicity of applicant",
            "2018",
            dateFormat.format(new java.util.Date()),
            msa,
            totalGrouping
          )
        }
      }
      .toList
  }
}
