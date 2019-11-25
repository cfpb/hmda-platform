package com.hmda.reports.processing

import akka.Done
import akka.actor.{ Actor, ActorLogging, Props }
import akka.stream._
import akka.stream.scaladsl._
import akka.pattern.pipe
import akka.stream.alpakka.s3.S3Settings
import com.hmda.reports.model._
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.spark.sql.{ SparkSession, _ }
import org.apache.spark.sql.functions._

import scala.concurrent._
import scala.util.{ Failure, Success, Try }

class AggregateProcessing(spark: SparkSession, s3Settings: S3Settings) extends Actor with ActorLogging {

  import AggregateProcessing._

  implicit val mat: ActorMaterializer = ActorMaterializer()(context.system)
  implicit val ec: ExecutionContext   = context.dispatcher

  override def receive: Receive = {
    case ProcessAggregateKafkaRecord(lookupMap, jdbcUrl, bucket, year) =>
      val originalSender = sender()
      log.info(s"Beginning Aggregate Reports")
      processAggregateKafkaRecord(spark, lookupMap, jdbcUrl, bucket, year, s3Settings)
        .map(_ => Finished)
        .pipeTo(originalSender)
      log.info(s"Finished process for Aggregate Reports")

  }
}

object AggregateProcessing {

  case class ProcessAggregateKafkaRecord(lookupMap: Map[(Int, Int), StateMapping], jdbcUrl: String, bucket: String, year: String)
  case object Finished

  def props(sparkSession: SparkSession, s3Settings: S3Settings): Props =
    Props(new AggregateProcessing(sparkSession, s3Settings))

  def processAggregateKafkaRecord(
    spark: SparkSession,
    lookupMap: Map[(Int, Int), StateMapping],
    jdbcUrl: String,
    bucket: String,
    year: String,
    s3Settings: S3Settings
  )(implicit mat: ActorMaterializer, ec: ExecutionContext): Future[Unit] = {
    
    import spark.implicits._

    def cachedRecordsDf: DataFrame =
      spark.read
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", jdbcUrl)
        .option("numPartitions", 1000)
        .option("partitionColumn", "msa_md")
        .option("lowerBound", 0)
        .option("upperBound", 99999)
        .option(
          "dbtable",
          s"(select * from modifiedlar2018_snapshot where filing_year = $year and state <> 'NA' and county <> 'NA' and lei not in ('BANK1LEIFORTEST12345','BANK3LEIFORTEST12345','BANK4LEIFORTEST12345','999999LE3ZOZXUS7W648','28133080042813308004','B90YWS6AFX2LGWOXJ1LD')) as mlar"
        )
        .load()
        .withColumnRenamed("race_categorization", "race")
        .withColumnRenamed("ethnicity_categorization", "ethnicity")
        .withColumnRenamed("sex_categorization", "sex")
        .cache()

    val cachedRecordsInstitions2018: DataFrame =
      spark.read
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", jdbcUrl)
        .option(
          "dbtable",
          s"(select lei as institution_lei, respondent_name from institutions2018_snapshot where hmda_filer = true) as institutions2018"
        )
        .load()
        .cache()

    def jsonFormationTable9(msaMd: Msa, input: List[DataMedAge]): OutAggregateMedAge = {
      val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")
      val medianAges = input
        .groupBy(d => d.msa_md)
        .flatMap {
          case (msa, datasByMsa) =>
            val medianAges: List[MedianAge] = datasByMsa
              .groupBy(_.median_age_calculated)
              .map {
                case (medianAge, datasByMedianAges) =>
                  val dispositions: List[DispositionMedAge] = datasByMedianAges
                    .groupBy(d => d.dispositionName)
                    .map {
                      case (dispositionName, datasByDispositionName) =>
                        val listInfo: List[InfoMedAge] = datasByDispositionName
                          .map(d => InfoMedAge(d.title, d.count, d.loan_amount))
                        DispositionMedAge(dispositionName, listInfo)
                    }
                    .toList
                    .sorted
                  BaseProcessing.buildSortedMedAge(MedianAge(medianAge, dispositions, "unsorted"))
              }
              .toList
              .sorted
            medianAges
        }
        .toList
      OutAggregateMedAge(
        "9",
        "Aggregate",
        "Disposition of loan applications, by median age of homes in census tract in which property is located and type of loan",
        year,
        dateFormat.format(new java.util.Date()),
        msaMd,
        "Census Tracts by Median Age of Homes",
        medianAges
      )
    }

    def jsonFormationAggregateTable1(msaMd: Msa, input: List[AggregateData]): OutAggregate1 = {
      val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")

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
                        Disposition(title.split("-")(0).trim, listInfo, title)
                    }
                    .toList
                    .sorted
                  val stateCode      = Try(tract.take(2).toInt).getOrElse(-1)
                  val countyCode     = Try(tract.slice(2, 5).toInt).getOrElse(-1)
                  val remainingTract = tract.drop(5)
                  val stateMapping =
                    lookupMap.getOrElse((stateCode, countyCode), StateMapping())
                  Tract(stateMapping.county + "/" + stateMapping.stateName + "/" + remainingTract, dispositions)
              }
              .toList
            tracts
        }
        .toList
      OutAggregate1(
        "1",
        "Aggregate",
        "Disposition of loan applications, by location of property and type of loan",
        year.toInt,
        dateFormat.format(new java.util.Date()),
        msaMd,
        tracts
      )
    }

    def jsonFormationAggregateTable2(msaMd: Msa, input: List[AggregateData]): OutAggregate2 = {
      val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")

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
                        Disposition(title.split("-")(0).trim, listInfo, title)
                    }
                    .toList
                    .sorted
                  val stateCode      = Try(tract.take(2).toInt).getOrElse(-1)
                  val countyCode     = Try(tract.slice(2, 5).toInt).getOrElse(-1)
                  val remainingTract = tract.drop(5)
                  val stateMapping =
                    lookupMap.getOrElse((stateCode, countyCode), StateMapping())
                  Tract2(stateMapping.county + "/" + stateMapping.stateName + "/" + remainingTract, dispositions(0).values)
                //                  Tract2("", dispositions(0).values)
              }
              .toList
            tracts
        }
        .toList

      OutAggregate2(
        "2",
        "Aggregate",
        "Loans purchased, by location of property and type of loan",
        year.toInt,
        dateFormat.format(new java.util.Date()),
        msaMd,
        tracts
      )
    }

    def buildDisposition(input: List[DataRaceEthnicity], title: String): DispositionRaceEthnicity =
      input.foldLeft(DispositionRaceEthnicity(title, 0, 0, title)) {
        case (DispositionRaceEthnicity(name, curCount, curValue, nameForSorting), next) =>
          DispositionRaceEthnicity(name.split("-")(0).trim, curCount + next.count, curValue + next.loan_amount, nameForSorting)
      }

    def jsonTransformationReportByEthnicityThenGender(input: List[DataRaceEthnicity]): List[ReportByEthnicityThenGender] = {
      val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")
      input
        .groupBy(data => (data.msa_md, data.msa_md_name))
        .map {
          case ((msa_md, msa_md_name), dataForMsa: List[DataRaceEthnicity]) =>
            val totalGrouping: List[Ethnicity] = dataForMsa
              .groupBy(_.ethnicity)
              .map {
                case (eachEthnicity, dataForEthnicity: List[DataRaceEthnicity]) =>
                  val dispositionsByEthnicity: List[DispositionRaceEthnicity] =
                    dataForEthnicity
                      .groupBy(_.title)
                      .map {
                        case (eachDisposition: String, dataForDisposition: List[DataRaceEthnicity]) =>
                          buildDisposition(dataForDisposition, eachDisposition)
                      }
                      .toList
                      .sorted

                  val dispositionByEthnicityAndGender: List[Gender] =
                    dataForEthnicity
                      .groupBy(_.sex)
                      .map {
                        case (eachGender: String, dataForGender: List[DataRaceEthnicity]) =>
                          val dispositionsForGender: List[DispositionRaceEthnicity] =
                            dataForGender
                              .groupBy(_.title)
                              .map {
                                case (eachDisposition: String, dataForDisposition: List[DataRaceEthnicity]) =>
                                  buildDisposition(dataForDisposition, eachDisposition)
                              }
                              .toList
                              .sorted
                          BaseProcessing.buildSortedGender(Gender(eachGender, dispositionsForGender, "unsorted"))
                      }
                      .toList
                      .sorted
                  BaseProcessing.buildSortedEthnicity(
                    Ethnicity(eachEthnicity, dispositionsByEthnicity, dispositionByEthnicityAndGender, "unsorted")
                  )

              }
              .toList
              .sorted
            val msa = Msa(msa_md.toString(), msa_md_name, "", "")
            ReportByEthnicityThenGender(
              "4",
              "Aggregate",
              "Disposition of loan applications, by ethnicity and sex of applicant",
              year,
              dateFormat.format(new java.util.Date()),
              msa,
              totalGrouping
            )
        }
        .toList
    }

    def jsonFormationRaceThenGender(input: List[DataRaceEthnicity]): List[ReportByRaceThenGender] = {
      val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")
      input
        .groupBy(data => (data.msa_md, data.msa_md_name))
        .map {
          case ((msa_md, msa_md_name), dataForMsa: List[DataRaceEthnicity]) =>
            val totalGrouping: List[Race] = dataForMsa
              .groupBy(_.race)
              .map {
                case (eachRace, dataForRace: List[DataRaceEthnicity]) =>
                  val dispositionsByRace: List[DispositionRaceEthnicity] =
                    dataForRace
                      .groupBy(_.title)
                      .map {
                        case (eachDisposition: String, dataForDisposition: List[DataRaceEthnicity]) =>
                          buildDisposition(dataForDisposition, eachDisposition)
                      }
                      .toList
                      .sorted

                  val dispositionByRaceAndGender: List[Gender] =
                    dataForRace
                      .groupBy(_.sex)
                      .map {
                        case (eachGender: String, dataForGender: List[DataRaceEthnicity]) =>
                          val dispositionsForGender: List[DispositionRaceEthnicity] =
                            dataForGender
                              .groupBy(_.title)
                              .map {
                                case (eachDisposition: String, dataForDisposition: List[DataRaceEthnicity]) =>
                                  buildDisposition(dataForDisposition, eachDisposition)
                              }
                              .toList
                              .sorted
                          BaseProcessing.buildSortedGender(Gender(eachGender, dispositionsForGender, "unsorted"))
                      }
                      .toList
                      .sorted

                  BaseProcessing.buildSortedRace(Race(eachRace, dispositionsByRace, dispositionByRaceAndGender, "unsorted"))
              }
              .toList
              .sorted
            val msa = Msa(msa_md.toString(), msa_md_name, "", "")
            ReportByRaceThenGender(
              "3",
              "Aggregate",
              "Disposition of loan applications, by race and sex of applicant",
              year,
              dateFormat.format(new java.util.Date()),
              msa,
              totalGrouping
            )
        }
        .toList
    }

    def persistJson(input: List[OutAggregate1]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(s"$bucket/reports/aggregate/$year/${input.msa.id}/1.json", data, "cfpb-hmda-public", s3Settings)(
            mat,
            ec
          )
        }
        .runWith(Sink.ignore)

    def persistJson2(input: List[OutAggregate2]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(s"$bucket/reports/aggregate/$year/${input.msa.id}/2.json", data, "cfpb-hmda-public", s3Settings)(
            mat,
            ec
          )
        }
        .runWith(Sink.ignore)

    def persistJson9(input: List[OutAggregateMedAge]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(s"$bucket/reports/aggregate/$year/${input.msa.id}/9.json", data, "cfpb-hmda-public", s3Settings)(
            mat,
            ec
          )
        }
        .runWith(Sink.ignore)

    def persistJsonI(input: List[OutReportedInstitutions]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(s"$bucket/reports/aggregate/$year/${input.msa.id}/i.json", data, "cfpb-hmda-public", s3Settings)(
            mat,
            ec
          )
        }
        .runWith(Sink.ignore)

    def persistJsonEthnicitySex(input: List[ReportByEthnicityThenGender]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(s"$bucket/reports/aggregate/$year/${input.msa.id}/4.json", data, "cfpb-hmda-public", s3Settings)(
            mat,
            ec
          )
        }
        .runWith(Sink.ignore)

    def persistJsonRaceSex(input: List[ReportByRaceThenGender]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(s"$bucket/reports/aggregate/$year/${input.msa.id}/3.json", data, "cfpb-hmda-public", s3Settings)(
            mat,
            ec
          )
        }
        .runWith(Sink.ignore)

    def aggregateTable1: List[OutAggregate1] =
      BaseProcessing
        .outputCollectionTable1(cachedRecordsDf, spark)
        .groupBy(d => d.msa_md)
        .map {
          case (key, values) =>
            val msaMd = Msa(key.toString, values.head.msa_md_name, "", "")
            jsonFormationAggregateTable1(msaMd, values)
        }
        .toList

    def aggregateTable2: List[OutAggregate2] =
      BaseProcessing
        .outputCollectionTable2(cachedRecordsDf, spark)
        .groupBy(d => d.msa_md)
        .map {
          case (key, values) =>
            val msaMd = Msa(key.toString, values.head.msa_md_name, "", "")
            jsonFormationAggregateTable2(msaMd, values)
        }
        .toList

    def aggregateTable9: List[OutAggregateMedAge] =
      MedianAgeProcessing
        .outputCollectionTable1(cachedRecordsDf, spark)
        .groupBy(d => d.msa_md)
        .map {
          case (key, values) =>
            val msaMd = Msa(key.toString(), values.head.msa_md_name, "", "")
            jsonFormationTable9(msaMd, values)
        }
        .toList

    def reportedInstitutions() = {
      import spark.implicits._
      val clonedRenamed = cachedRecordsInstitions2018
        .withColumnRenamed("institution_lei", "institution_lei")
        .withColumnRenamed("respondent_name", "respondent_name")
      val clonedDf = cachedRecordsDf
        .withColumnRenamed("lei", "mlar_lei")
      clonedDf
        .join(
          clonedRenamed,
          clonedRenamed
            .col("institution_lei") === clonedDf.col("mlar_lei"),
          "inner"
        )
        .groupBy(col("msa_md"), col("msa_md_name"))
        .agg(collect_set(col("respondent_name")) as "reported_institutions")
        .as[ReportedInstitutions]
        .collect
        .toSet
    }

    val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")
    def aggregateTableI = reportedInstitutions.groupBy(d => d.msa_md).map {
      case (msa, institutions) =>
        val msaMd: Msa =
          Msa(msa.toString(), institutions.head.msa_md_name, "", "")
        val reportedInstitutions: Set[String] =
          institutions.flatMap(i => i.reported_institutions).map(_.toUpperCase)
        println(reportedInstitutions)
        OutReportedInstitutions(
          "I",
          "Aggregate",
          "List of financial institutions whose data make up the 2018 MSA/MD aggregate report",
          year,
          dateFormat.format(new java.util.Date()),
          msaMd,
          reportedInstitutions
        )
    }

    def persistIncomeRaceEthnicity(input: List[ReportByApplicantIncome]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(s"$bucket/reports/aggregate/$year/${input.msa.id}/5.json", data, "cfpb-hmda-public", s3Settings)(
            mat,
            ec
          )
        }
        .runWith(Sink.ignore)

    val result = for {
      _ <- persistJson(aggregateTable1)
      _ <- persistJson2(aggregateTable2)
      _ <- persistJson9(aggregateTable9)
      _ <- persistJsonI(aggregateTableI.toList)
      _ <- persistJsonRaceSex(jsonFormationRaceThenGender(RaceGenderProcessing.outputCollectionTable3and4(cachedRecordsDf, spark)))
      _ <- persistJsonEthnicitySex(
            jsonTransformationReportByEthnicityThenGender(RaceGenderProcessing.outputCollectionTable3and4(cachedRecordsDf, spark))
          )
      _ <- persistIncomeRaceEthnicity(
            IncomeRaceEthnicityProcessing.jsonFormationApplicantIncome(
              IncomeRaceEthnicityProcessing
                .outputCollectionTableIncome(cachedRecordsDf, spark)
            )
          )
    } yield ()

    result.onComplete {
      case Success(_) => println(s"Finished Aggregate Reports")
      case Failure(exception) =>
        println(s"Exception happened when processing Aggregate Reports" + exception.getMessage)
        println("Printing stacktrace")
        exception.printStackTrace()
    }

    result
  }
}
