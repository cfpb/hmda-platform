package com.hmda.reports.processing

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import akka.stream._
import akka.stream.alpakka.s3.scaladsl.{MultipartUploadResult, S3Client}
import akka.stream.scaladsl._
import akka.pattern.pipe
import akka.util.ByteString
import com.hmda.reports.model._
import hmda.model.census.{Census, State}
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.spark.sql.{SparkSession, _}
import org.apache.spark.sql.functions._

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class DisclosureProcessing(spark: SparkSession, s3Client: S3Client)
    extends Actor
    with ActorLogging {
  import DisclosureProcessing._

  implicit val mat: ActorMaterializer = ActorMaterializer()(context.system)
  implicit val ec: ExecutionContext = context.dispatcher

  override def receive: Receive = {
    case ProcessDisclosureKafkaRecord(lei, lookupMap, jdbcUrl, bucket, year) =>
      val originalSender = sender()
      log.info(s"Beginning process for $lei")
      processDisclosureKafkaRecord(lei,
                                   spark,
                                   lookupMap,
                                   jdbcUrl,
                                   bucket,
                                   year,
                                   s3Client)
        .map(_ => Finished)
        .pipeTo(originalSender)
      log.info(s"Finished process for $lei")

  }
}

object DisclosureProcessing {
  case class ProcessDisclosureKafkaRecord(
      lei: String,
      lookupMap: Map[(Int, Int), StateMapping],
      jdbcUrl: String,
      bucket: String,
      year: String)
  case object Finished

  def props(sparkSession: SparkSession, s3Client: S3Client): Props =
    Props(new DisclosureProcessing(sparkSession, s3Client))

  def processDisclosureKafkaRecord(lei: String,
                                   spark: SparkSession,
                                   lookupMap: Map[(Int, Int), StateMapping],
                                   jdbcUrl: String,
                                   bucket: String,
                                   year: String,
                                   s3Client: S3Client)(
      implicit mat: ActorMaterializer,
      ec: ExecutionContext): Future[Unit] = {
    import spark.implicits._

    def prepare(df: DataFrame): DataFrame =
      df.filter(col("msa_md") =!= lit(0))
        .filter(upper(col("tract")) =!= lit("NA"))
        .filter(upper(col("filing_year")) === lit(year))

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
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
      val dispB = prepare(input)
        .filter(col("action_taken_type").isin(actionsTaken: _*))
        .filter(col("total_units") isin ("1", "2", "3", "4"))
        .filter(col("loan_purpose") === lit(1))
        .filter(col("loan_type") === lit(1))
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
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
        .groupBy(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
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
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
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
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
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
                     allUniqueMsaMdTract: DataFrame): Dataset[Data] = {
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

    def jsonFormationTable1(msaMd: Msa,
                            input: List[Data],
                            leiDetails: Institution): OutDisclosure1 = {
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
                //                  Tract("", dispositions)
              }
              .toList
            tracts
        }
        .toList
      OutDisclosure1(
        leiDetails.lei,
        leiDetails.institutionName,
        "1",
        "Disclosure",
        "Disposition of loan applications, by location of property and type of loan",
        year.toInt,
        dateFormat.format(new java.util.Date()),
        msaMd,
        tracts
      )
    }

    def jsonFormationTable2(msaMd: Msa,
                            input: List[Data],
                            leiDetails: Institution): OutDisclosure2 = {
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

      // Disclosure2(msaMd, tracts)
      OutDisclosure2(
        leiDetails.lei,
        leiDetails.institutionName,
        "2",
        "Disclosure",
        "Loans purchased, by location of property and type of loan",
        year.toInt,
        dateFormat.format(new java.util.Date()),
        msaMd,
        tracts
      )
    }

    def persistSingleFile(fileName: String,
                          data: String,
                          s3Bucket: String): Future[MultipartUploadResult] =
      Source
        .single(data)
        .map(ByteString(_))
        .runWith(s3Client.multipartUpload(s3Bucket, fileName))

    def persistJson(input: List[OutDisclosure1]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          persistSingleFile(
            s"$bucket/reports/disclosure/$year/$lei/${input.msa.id}/1.json",
            data,
            "cfpb-hmda-public")
        }
        .runWith(Sink.ignore)

    def persistJson2(input: List[OutDisclosure2]): Future[Done] =
      Source(input)
        .mapAsyncUnordered(10) { input =>
          val data: String = input.asJson.noSpaces
          persistSingleFile(
            s"$bucket/reports/disclosure/$year/$lei/${input.msa.id}/2.json",
            data,
            "cfpb-hmda-public")
        }
        .runWith(Sink.ignore)

    def leiDetails: Institution =
      spark.read
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", jdbcUrl)
        .option(
          "dbtable",
          s"(select lei, respondent_name as institutionName from institutions2018 where lei = '$lei' and hmda_filer = true) as institutions2018")
        .load()
        .as[Institution]
        .collect()
        .head

    def cachedRecordsDf: DataFrame =
      spark.read
        .format("jdbc")
        .option("driver", "org.postgresql.Driver")
        .option("url", jdbcUrl)
        .option(
          "dbtable",
          s"(select * from modifiedlar2018 where lei = '$lei' and filing_year = $year) as mlar")
        .load()
        .cache()

    def allUniqueMsaMdTract =
      cachedRecordsDf
        .select(col("tract"), col("msa_md"), col("msa_md_name"), col("state"))
        .dropDuplicates()
        .cache()

    def outputCollectionTable1(allUniqueMsaMdTract: DataFrame,
                               cachedRecordsDf: DataFrame): List[Data] = {
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
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputBTable1 = actionsTakenTable1
        .map {
          case (description, eachList) =>
            dispositionB(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputCTable1 = actionsTakenTable1
        .map {
          case (description, eachList) =>
            dispositionC(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputDTable1 = actionsTakenTable1
        .map {
          case (description, eachList) =>
            dispositionD(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputETable1 = actionsTakenTable1
        .map {
          case (description, eachList) =>
            dispositionE(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputFTable1 = actionsTakenTable1
        .map {
          case (description, eachList) =>
            dispositionF(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputGTable1 = actionsTakenTable1
        .map {
          case (description, eachList) =>
            dispositionG(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      spark.sparkContext.setJobGroup(lei, "Table 1 Disposition A")
      val aList = outputATable1.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 1 Disposition B")
      val bList = outputBTable1.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 1 Disposition C")
      val cList = outputCTable1.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 1 Disposition D")
      val dList = outputDTable1.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 1 Disposition E")
      val eList = outputETable1.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 1 Disposition F")
      val fList = outputFTable1.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 1 Disposition G")
      val gList = outputGTable1.collect().toList

      aList ++ bList ++ cList ++ dList ++ eList ++ fList ++ gList
    }

    def outputCollectionTable2(allUniqueMsaMdTract: DataFrame): List[Data] = {
      val actionsTakenTable2 = Map(
        "Purchased Loans" -> List(6)
      )

      val outputATable2 = actionsTakenTable2
        .map {
          case (description, eachList) =>
            dispositionA(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputBTable2 = actionsTakenTable2
        .map {
          case (description, eachList) =>
            dispositionB(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputCTable2 = actionsTakenTable2
        .map {
          case (description, eachList) =>
            dispositionC(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputDTable2 = actionsTakenTable2
        .map {
          case (description, eachList) =>
            dispositionD(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputETable2 = actionsTakenTable2
        .map {
          case (description, eachList) =>
            dispositionE(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputFTable2 = actionsTakenTable2
        .map {
          case (description, eachList) =>
            dispositionF(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      val outputGTable2 = actionsTakenTable2
        .map {
          case (description, eachList) =>
            dispositionG(cachedRecordsDf,
                         description,
                         eachList,
                         allUniqueMsaMdTract)
        }
        .reduce(_ union _)

      spark.sparkContext.setJobGroup(lei, "Table 2 Disposition A")
      val aList = outputATable2.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 2 Disposition B")
      val bList = outputBTable2.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 2 Disposition C")
      val cList = outputCTable2.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 2 Disposition D")
      val dList = outputDTable2.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 2 Disposition E")
      val eList = outputETable2.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 2 Disposition F")
      val fList = outputFTable2.collect().toList
      spark.sparkContext.setJobGroup(lei, "Table 2 Disposition G")
      val gList = outputGTable2.collect().toList

      aList ++ bList ++ cList ++ dList ++ eList ++ fList ++ gList
    }

    def disclosuresTable1: List[OutDisclosure1] =
      outputCollectionTable1(allUniqueMsaMdTract, cachedRecordsDf)
        .groupBy(d => d.msa_md)
        .map {
          case (key, values) =>
            val msaMd = Msa(
              key.toString,
              values.head.msa_md_name,
              values.head.state,
              Census.states.getOrElse(values.head.state, State("", "")).name)
            jsonFormationTable1(msaMd, values, leiDetails)
        }
        .toList

    def disclosuresTable2: List[OutDisclosure2] =
      outputCollectionTable2(allUniqueMsaMdTract)
        .groupBy(d => d.msa_md)
        .map {
          case (key, values) =>
            val msaMd = Msa(
              key.toString,
              values.head.msa_md_name,
              values.head.state,
              Census.states.getOrElse(values.head.state, State("", "")).name)
            jsonFormationTable2(msaMd, values, leiDetails)
        }
        .toList

    val result = for {
      _ <- persistJson(disclosuresTable1)
      _ <- persistJson2(disclosuresTable2)
    } yield ()

    result.onComplete {
      case Success(_) => println(s"Finished processing LEI: $lei")
      case Failure(exception) =>
        println(
          s"Exception happened when processing LEI $lei" + exception.getMessage)
        println("Printing stacktrace")
        exception.printStackTrace()
    }

    result
  }
}
