package com.hmda.reports.processing

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream._
import akka.stream.scaladsl._
import akka.pattern.pipe
import akka.stream.alpakka.s3.S3Settings
import akka.util.ByteString
import com.hmda.reports.model._
import hmda.model.census.{Census, State}
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.spark.sql.{SparkSession, _}
import org.apache.spark.sql.functions._

import scala.concurrent._
import scala.util.{Failure, Success, Try}

class AggregateProcessing(spark: SparkSession, s3Settings: S3Settings)
    extends Actor
    with ActorLogging {

  import AggregateProcessing._

  implicit val mat: ActorMaterializer = ActorMaterializer()(context.system)
  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case ProcessAggregateKafkaRecord(lookupMap, jdbcUrl, bucket, year) =>
      val originalSender = sender()
      log.info(s"Beginning Aggregate Reports")
      processAggregateKafkaRecord(spark,
                                  lookupMap,
                                  jdbcUrl,
                                  bucket,
                                  year,
                                  s3Settings)
        .map(_ => Finished)
        .pipeTo(originalSender)
      log.info(s"Finished process for Aggregate Reports")

  }
}

object AggregateProcessing {
  case class ProcessAggregateKafkaRecord(
      lookupMap: Map[(Int, Int), StateMapping],
      jdbcUrl: String,
      bucket: String,
      year: String)
  case object Finished

  def props(sparkSession: SparkSession, s3Settings: S3Settings): Props =
    Props(new AggregateProcessing(sparkSession, s3Settings))

  def processAggregateKafkaRecord(spark: SparkSession,
                                  lookupMap: Map[(Int, Int), StateMapping],
                                  jdbcUrl: String,
                                  bucket: String,
                                  year: String,
                                  s3Settings: S3Settings)(
      implicit mat: ActorMaterializer,
      ec: ExecutionContext): Future[Unit] = {

    import spark.implicits._

    def cachedRecordsDf: DataFrame =
      spark.read
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", jdbcUrl)
        .option(
          "dbtable",
          s"(select * from modifiedlar2018 where filing_year = $year) as mlar")
        .load()
        .withColumnRenamed("race_categorization", "race")
        .withColumnRenamed("sex_categorization", "sex")
        .withColumnRenamed("ethnicity_categorization", "ethnicity")
        .cache()

    val cachedRecordsInstitions2018: DataFrame =
      spark.read
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", jdbcUrl)
        .option(
          "dbtable",
          s"(select lei as institution_lei, respondent_name from institutions2018 where hmda_filer = true) as institutions2018")
        .load()
        .cache()

    def jsonFormationTable9(msaMd: Msa,
                            input: List[DataMedAge]): OutAggregateMedAge = {
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
                  MedianAge(medianAge, dispositions)
              }
              .toList
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

    def jsonFormationAggregateTable1(msaMd: Msa,
                                     input: List[Data]): OutAggregate1 = {
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
                        val listInfo: List[Info] = datasByTitle.map(d =>
                          Info(d.dispositionName, d.count, d.loan_amount))
                        Disposition(title, listInfo)
                    }
                    .toList
                  val stateCode = Try(tract.take(2).toInt).getOrElse(-1)
                  val countyCode = Try(tract.slice(2, 5).toInt).getOrElse(-1)
                  val remainingTract = tract.drop(5)
                  val stateMapping =
                    lookupMap.getOrElse((stateCode, countyCode), StateMapping())
                  Tract(
                    stateMapping.county + "/" + stateMapping.stateName + "/" + remainingTract,
                    dispositions)
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

    def jsonFormationAggregateTable2(msaMd: Msa,
                                     input: List[Data]): OutAggregate2 = {
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
                        val listInfo: List[Info] = datasByTitle.map(d =>
                          Info(d.dispositionName, d.count, d.loan_amount))
                        Disposition(title, listInfo)
                    }
                    .toList
                  val stateCode = Try(tract.take(2).toInt).getOrElse(-1)
                  val countyCode = Try(tract.slice(2, 5).toInt).getOrElse(-1)
                  val remainingTract = tract.drop(5)
                  val stateMapping =
                    lookupMap.getOrElse((stateCode, countyCode), StateMapping())
                  Tract2(
                    stateMapping.county + "/" + stateMapping.stateName + "/" + remainingTract,
                    dispositions(0).values)
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

    def buildDisposition(input: List[DataRaceEthnicity],
                         dispositionName: String): DispositionRaceEthnicity =
      input.foldLeft(DispositionRaceEthnicity(dispositionName, 0, 0)) {
        case (DispositionRaceEthnicity(name, curCount, curValue), next) =>
          DispositionRaceEthnicity(name,
                                   curCount + next.count,
                                   curValue + next.loan_amount)
      }

    def jsonTransformationReportByEthnicityThenGender(
        input: List[DataRaceEthnicity]): List[ReportByEthnicityThenGender] = {
      val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")
      input
        .groupBy(data => (data.msa_md, data.msa_md_name, data.state))
        .map {
          case ((msa_md, msa_md_name, state),
                dataForMsa: List[DataRaceEthnicity]) =>
            val totalGrouping: List[Ethnicity] = dataForMsa
              .groupBy(_.ethnicity)
              .map {
                case (eachEthnicity,
                      dataForEthnicity: List[DataRaceEthnicity]) =>
                  val dispositionsByEthnicity: List[DispositionRaceEthnicity] =
                    dataForEthnicity
                      .groupBy(_.dispositionName)
                      .map {
                        case (eachDisposition: String,
                              dataForDisposition: List[DataRaceEthnicity]) =>
                          buildDisposition(dataForDisposition, eachDisposition)
                      }
                      .toList

                  val dispositionByEthnicityAndGender: List[Gender] =
                    dataForEthnicity
                      .groupBy(_.sex)
                      .map {
                        case (eachGender: String,
                              dataForGender: List[DataRaceEthnicity]) =>
                          val dispositionsForGender
                            : List[DispositionRaceEthnicity] =
                            dataForGender
                              .groupBy(_.dispositionName)
                              .map {
                                case (eachDisposition: String,
                                      dataForDisposition: List[
                                        DataRaceEthnicity]) =>
                                  buildDisposition(dataForDisposition,
                                                   eachDisposition)
                              }
                              .toList

                          Gender(eachGender, dispositionsForGender)
                      }
                      .toList

                  Ethnicity(eachEthnicity,
                            dispositionsByEthnicity,
                            dispositionByEthnicityAndGender)
              }
              .toList
            val msa = Msa(msa_md.toString(),
                          msa_md_name,
                          state,
                          Census.states.getOrElse(state, State("", "")).name)
            ReportByEthnicityThenGender(
              "4",
              "Aggregate",
              "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4- family and manufactured home dwellings, by race and gender",
              year,
              dateFormat.format(new java.util.Date()),
              msa,
              totalGrouping
            )
        }
        .toList
    }

    def jsonFormationRaceThenGender(
        input: List[DataRaceEthnicity]): List[ReportByRaceThenGender] = {
      val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")
      input
        .groupBy(data => (data.msa_md, data.msa_md_name, data.state))
        .map {
          case ((msa_md, msa_md_name, state),
                dataForMsa: List[DataRaceEthnicity]) =>
            val totalGrouping: List[Race] = dataForMsa
              .groupBy(_.race)
              .map {
                case (eachRace, dataForRace: List[DataRaceEthnicity]) =>
                  val dispositionsByRace: List[DispositionRaceEthnicity] =
                    dataForRace
                      .groupBy(_.dispositionName)
                      .map {
                        case (eachDisposition: String,
                              dataForDisposition: List[DataRaceEthnicity]) =>
                          buildDisposition(dataForDisposition, eachDisposition)
                      }
                      .toList

                  val dispositionByRaceAndGender: List[Gender] =
                    dataForRace
                      .groupBy(_.sex)
                      .map {
                        case (eachGender: String,
                              dataForGender: List[DataRaceEthnicity]) =>
                          val dispositionsForGender
                            : List[DispositionRaceEthnicity] =
                            dataForGender
                              .groupBy(_.dispositionName)
                              .map {
                                case (eachDisposition: String,
                                      dataForDisposition: List[
                                        DataRaceEthnicity]) =>
                                  buildDisposition(dataForDisposition,
                                                   eachDisposition)
                              }
                              .toList

                          Gender(eachGender, dispositionsForGender)
                      }
                      .toList

                  Race(eachRace, dispositionsByRace, dispositionByRaceAndGender)
              }
              .toList
            val msa = Msa(msa_md.toString(),
                          msa_md_name,
                          state,
                          Census.states.getOrElse(state, State("", "")).name)
            ReportByRaceThenGender(
              "3",
              "Aggregate",
              "Disposition of applications for FHA, FSA/RHS, and VA home-purchase loans, 1- to 4- family and manufactured home dwellings, by race and gender",
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
          BaseProcessing.persistSingleFile(
            s"$bucket/reports/aggregate/$year/${input.msa.id}/1.json",
            data,
            "cfpb-hmda-public",
            s3Settings)(mat, ec)
        }
        .runWith(Sink.ignore)

    def persistJson2(input: List[OutAggregate2]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(
            s"$bucket/reports/aggregate/$year/${input.msa.id}/2.json",
            data,
            "cfpb-hmda-public",
            s3Settings)(mat, ec)
        }
        .runWith(Sink.ignore)

    def persistJson9(input: List[OutAggregateMedAge]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(
            s"$bucket/reports/aggregate/$year/${input.msa.id}/9.json",
            data,
            "cfpb-hmda-public",
            s3Settings)(mat, ec)
        }
        .runWith(Sink.ignore)

    def persistJsonI(input: List[OutReportedInstitutions]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(
            s"$bucket/reports/aggregate/2018/${input.msa.id}/i.json",
            data,
            "cfpb-hmda-public",
            s3Settings)(mat, ec)
        }
        .runWith(Sink.ignore)

    def persistJsonEthnicitySex(
        input: List[ReportByEthnicityThenGender]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(
            s"dev/reports/aggregate/2018/${input.msa.id}/4.json",
            data,
            "cfpb-hmda-public",
            s3Settings)(mat, ec)
        }
        .runWith(Sink.ignore)

    def persistJsonRaceSex(input: List[ReportByRaceThenGender]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          BaseProcessing.persistSingleFile(
            s"dev/reports/aggregate/2018/${input.msa.id}/3.json",
            data,
            "cfpb-hmda-public",
            s3Settings)(mat, ec)
        }
        .runWith(Sink.ignore)

    def aggregateTable1: List[OutAggregate1] =
      BaseProcessing
        .outputCollectionTable1(cachedRecordsDf, spark)
        .groupBy(d => d.msa_md)
        .map {
          case (key, values) =>
            val msaMd = Msa(
              key.toString,
              values.head.msa_md_name,
              values.head.state,
              Census.states.getOrElse(values.head.state, State("", "")).name)
            jsonFormationAggregateTable1(msaMd, values)
        }
        .toList

    def aggregateTable2: List[OutAggregate2] =
      BaseProcessing
        .outputCollectionTable2(cachedRecordsDf, spark)
        .groupBy(d => d.msa_md)
        .map {
          case (key, values) =>
            val msaMd = Msa(
              key.toString,
              values.head.msa_md_name,
              values.head.state,
              Census.states.getOrElse(values.head.state, State("", "")).name)
            jsonFormationAggregateTable2(msaMd, values)
        }
        .toList

    def aggregateTable9: List[OutAggregateMedAge] =
      MedianAgeProcessing
        .outputCollectionTable1(cachedRecordsDf, spark)
        .groupBy(d => d.msa_md)
        .map {
          case (key, values) =>
            val msaMd = Msa(
              key.toString(),
              values.head.msa_md_name,
              values.head.state,
              Census.states.getOrElse(values.head.state, State("", "")).name)
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
        .join(clonedRenamed,
              clonedRenamed
                .col("institution_lei") === clonedDf.col("mlar_lei"),
              "inner")
        .groupBy(col("msa_md"), col("msa_md_name"), col("state"))
        .agg(collect_set(col("respondent_name")) as "reported_institutions")
        .as[ReportedInstitutions]
        .collect
        .toSet
    }

    val dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm aa")
    def aggregateTableI = reportedInstitutions.groupBy(d => d.msa_md).map {
      case (key, values) =>
        val msaMd: Msa =
          Msa(key.toString(),
              values.head.msa_md_name,
              values.head.state,
              Census.states.getOrElse(values.head.state, State("", "")).name)
        val institutions: Set[String] =
          values.map(d => d.reported_institutions.head)
        OutReportedInstitutions(
          "I",
          "Aggregate",
          "List of financial institutions whose data make up the 2018 MSA/MD aggregate report",
          year,
          dateFormat.format(new java.util.Date()),
          msaMd,
          institutions
        )
    }

    val result = for {
      _ <- persistJson(aggregateTable1)
      _ <- persistJson2(aggregateTable2)
      _ <- persistJson9(aggregateTable9)
      _ <- persistJsonI(aggregateTableI.toList)
      _ <- persistJsonRaceSex(
        jsonFormationRaceThenGender(
          RaceGenderProcessing.outputCollectionTable3(cachedRecordsDf, spark)))
      _ <- persistJsonEthnicitySex(
        jsonTransformationReportByEthnicityThenGender(
          RaceGenderProcessing.outputCollectionTable3(cachedRecordsDf, spark)))
    } yield ()

    result.onComplete {
      case Success(_) => println(s"Finished Aggregate Reports")
      case Failure(exception) =>
        println(
          s"Exception happened when processing Aggregate Reports" + exception.getMessage)
        println("Printing stacktrace")
        exception.printStackTrace()
    }

    result
  }
}
