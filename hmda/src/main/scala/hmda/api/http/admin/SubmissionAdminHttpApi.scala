package hmda.api.http.admin
// $COVERAGE-OFF$
import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.common.{CsvEntityStreamingSupport, EntityStreamingSupport}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.ContentTypes.`text/csv(UTF-8)`
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound, OK}
import akka.http.scaladsl.model.headers.ContentDispositionTypes.attachment
import akka.http.scaladsl.model.headers.`Content-Disposition`
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.{ByteString, Timeout}
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNec
import cats.implicits._
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.admin.SubmissionAdminHttpApi.{lineCount, pipeDelimitedFileStream, validateRawSubmissionId}
import hmda.api.http.model.admin.{LeiSubmissionSummaryResponse, SubmissionSummaryResponse, YearlySubmissionSummaryResponse}
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingCommands.{GetLatestSignedSubmission, GetOldestSignedSubmission}
import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionProcessingCommands.GetHmdaValidationErrorState
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.persistence.filing.FilingPersistence.selectFiling
import hmda.persistence.submission.HmdaValidationError.selectHmdaValidationError
import hmda.persistence.submission.{HmdaProcessingUtils, SubmissionPersistence}
import hmda.query.HmdaQuery
import hmda.utils.YearUtils
import hmda.utils.YearUtils.Period
import org.slf4j.Logger
import hmda.auth.AdminOnly

import scala.collection.immutable.ListMap
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object SubmissionAdminHttpApi {
  def create(log: Logger, config: Config, clusterSharding: ClusterSharding, countTimeout: Duration)(
    implicit t: Timeout,
    ec: ExecutionContext,
    system: ActorSystem[_],
    mat: Materializer
  ): OAuth2Authorization => Route =
    new SubmissionAdminHttpApi(log, config, clusterSharding, countTimeout)(t, ec, system, mat).routes
  /**
   * Reads the existing file from the journal
   * @param submissionId is the submission id corresponding to the file that was uploaded
   * @return
   */
  def pipeDelimitedFileStream(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Source[ByteString, NotUsed] =
    HmdaProcessingUtils
      .readRawData(submissionId)
      .take(1)
      .map(_.data)
      .map(tsLine => tsLine.replaceAll("(\r\n)|\r|\n", "")) // check for missing lei?
      .map(ByteString(_)) ++
      HmdaProcessingUtils
        .readRawData(submissionId)
        .drop(1) // drop the header which is the first line of the file
        .map(_.data)
        .map(singleLARLine => singleLARLine.replaceAll("(\r\n)|\r|\n", "")) // check for missing lei?
        .map(ByteString(_))

  def lineCount(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Future[Int] =
    HmdaProcessingUtils
      .readRawData(submissionId)
      .toMat(Sink.fold[Int, LineAdded](0)((acc, _) => acc + 1))(Keep.right)
      .run()

  def validateRawSubmissionId(rawSubmissionId: String): ValidatedNec[String, SubmissionId] = {
    def validateLEI(rawLei: String): ValidatedNec[String, String] =
      Try(rawLei).filter(_.nonEmpty).toEither.left.map(_ => "LEI is empty").toValidatedNec

    def validateYear(rawYear: String): ValidatedNec[String, Int] =
      Try(rawYear.toInt).toEither.left.map(_ => "year is not valid").toValidatedNec

    def validateSeqNr(rawSeqNr: String): ValidatedNec[String, Int] =
      Try(rawSeqNr.toInt).filter(nr => nr > 0).toEither.left.map(_ => "sequence number is not valid").toValidatedNec

    def validateQuarter(rawQuarter: String): ValidatedNec[String, String] =
      Try(rawQuarter).filter(YearUtils.isValidQuarter).toEither.left.map(_ => "quarter is not valid").toValidatedNec

    rawSubmissionId.split("-").toList match {
      case rawLei :: rawYear :: rawQuarter :: rawSequenceNumber :: Nil =>
        (validateLEI(rawLei), validateYear(rawYear), validateQuarter(rawQuarter), validateSeqNr(rawSequenceNumber)).mapN {
          case (lei, year, quarter, seqNr) =>
            SubmissionId(lei, Period(year, Some(quarter)), seqNr)
        }

      case rawLei :: rawYear :: rawSequenceNumber :: Nil =>
        (validateLEI(rawLei), validateYear(rawYear), validateSeqNr(rawSequenceNumber)).mapN {
          case (lei, year, seqNr) =>
            SubmissionId(lei, Period(year, None), seqNr)
        }

      case _ =>
        "You have entered an invalid submission-id".invalidNec[SubmissionId]
    }
  }
}

private class LeiSubmissionSummary(log: Logger, clusterSharding: ClusterSharding)(
  implicit ec: ExecutionContext, system: ActorSystem[_], mat: Materializer, t: Timeout) {

  private def submissionIdsStream: Source[SubmissionId, NotUsed] = {
    val submissionPrefix = SubmissionPersistence.name + "-"
    HmdaQuery
      .readJournal(system)
      .currentPersistenceIds()
      .mapConcat { persistenceId =>
        println (persistenceId)
        if (persistenceId.startsWith(submissionPrefix)) {
          validateRawSubmissionId(persistenceId.stripPrefix(submissionPrefix)) match {
            case Valid(submissionId) => List(submissionId)
            case Invalid(e) =>
              log.error(s"Cannot parse submission id from persistence id: $persistenceId, because: $e")
              Nil
          }
        } else {
          Nil
        }
      }
  }
  //TODO: In this method we know we will only be sending ONE lei. But the way this method is coded at the moment it will keep looping over all Submissions even after finding the one that matches
  //TODO Continued: I feel this method can be optimized for running. It should stop at finding the first instance or maybe there is even a better way to do this?
  private def submissionIdsStreamForLei (lei: String): Source[SubmissionId, NotUsed] = {
    val submissionPrefix = SubmissionPersistence.name + "-"

    HmdaQuery
      .readJournal(system)
      .currentPersistenceIds()
      .mapConcat { persistenceId =>
        if (persistenceId.startsWith(submissionPrefix+lei)) {
          validateRawSubmissionId(persistenceId.stripPrefix(submissionPrefix)) match {
            case Valid(submissionId) => List(submissionId)
            case Invalid(e) =>
              log.error(s"Cannot parse submission id from persistence id: $persistenceId, because: $e")
              Nil
          }
        } else {
          Nil
        }
      }
  }

  private def submissionsForLei(lei: String): Future[Map[String, Iterable[SubmissionId]]] = {
    submissionIdsStreamForLei(lei).toMat(Sink.seq)(Keep.right).run().map { submissionIds =>
      log.info(s"Found submission ids count: ${submissionIds.size}")
      submissionIds.groupBy(_.lei)
    }
  }

  private def submissionsByLei: Future[Map[String, Iterable[SubmissionId]]] = {
    submissionIdsStream.toMat(Sink.seq)(Keep.right).run().map { submissionIds =>
      log.info(s"Found submission ids count: ${submissionIds.size}")
      submissionIds.groupBy(_.lei)
    }
  }

  private def submissionSummary(submissionId: SubmissionId): Future[Option[SubmissionSummaryResponse]] = {
    val submissionRef = SubmissionPersistence.selectSubmissionPersistence(clusterSharding, submissionId)
    (submissionRef ? GetSubmission).flatMap {
      case None => Future.successful(None)
      case Some(submission) =>
        for {
          lines <- SubmissionAdminHttpApi.lineCount(submissionId)
          hmdaValidationError = selectHmdaValidationError(clusterSharding, submissionId)
          edits <- hmdaValidationError ? (ref => GetHmdaValidationErrorState(submissionId, ref))
        } yield {
          Some(SubmissionSummaryResponse(submissionId.toString, submission.status.code, lines,
            edits.validity.size, edits.syntactical.size, edits.quality.size, edits.`macro`.size))
        }
    }
  }

  private def yearlySubmissionSummary(submissionIds: List[SubmissionId]): Future[YearlySubmissionSummaryResponse] = {
    Source(submissionIds)
      .mapAsync(1)(submissionSummary)
      .collect {
        case Some(submissionSummary) => submissionSummary
      }
      .toMat(Sink.seq)(Keep.right)
      .run()
      .map(submissionSummaries => YearlySubmissionSummaryResponse(submissionIds.size, submissionSummaries.toList.sortBy(_.submissionId)))
  }

  def leiSubmissionSummaryStreamForLei(lei: String): Source[LeiSubmissionSummaryResponse, NotUsed] = {
    Source.future(submissionsForLei(lei))
      .mapConcat(identity)
      .mapAsync(1) { case (lei, submissionIds) =>
        log.info(s"For lei: $lei, found submission count: ${submissionIds.size}")

        Source(submissionIds.groupBy(_.period.year))
          .mapAsync(1) { case (year, submissionIds) => yearlySubmissionSummary(submissionIds.toList).map(year -> _) }
          .toMat(Sink.seq)(Keep.right)
          .run()
          .map(yearlySummaries => LeiSubmissionSummaryResponse(lei, ListMap(yearlySummaries.sortBy(_._1).map { case (k, v) => (k.toString, v) }: _*)))
      }
  }

  def leiSubmissionSummaryStream: Source[LeiSubmissionSummaryResponse, NotUsed] = {
    Source.future(submissionsByLei)
      .mapConcat(identity)
      .mapAsync(1) { case (lei, submissionIds) =>
        log.info(s"For lei: $lei, found submission count: ${submissionIds.size}")

        Source(submissionIds.groupBy(_.period.year))
          .mapAsync(1) { case (year, submissionIds) => yearlySubmissionSummary(submissionIds.toList).map(year -> _) }
          .toMat(Sink.seq)(Keep.right)
          .run()
          .map(yearlySummaries => LeiSubmissionSummaryResponse(lei, ListMap(yearlySummaries.sortBy(_._1).map { case (k, v) => (k.toString, v) }: _*)))
      }
  }
}

private class SubmissionAdminHttpApi(log: Logger, config: Config, clusterSharding: ClusterSharding, countTimeout: Duration)(
  implicit t: Timeout,
  ec: ExecutionContext,
  system: ActorSystem[_],
  mat: Materializer
) {
  private val hmdaAdminRole: String = config.getString("keycloak.hmda.admin.role")

  private implicit val csvStreamingSupport: CsvEntityStreamingSupport =
    EntityStreamingSupport.csv()

  val routes: OAuth2Authorization => Route = { (oAuth2Authorization: OAuth2Authorization) =>
    (get & path("institutions" / Segment / "signed" / "oldest"  / Segment )) { (lei, period) =>
      oAuth2Authorization.authorizeTokenWithRule(AdminOnly) { _ =>
        val fil = selectFiling(clusterSharding, lei, YearUtils.parsePeriod(period).right.get.year, YearUtils.parsePeriod(period).right.get.quarter)
        val fOldestSigned: Future[Option[Submission]] = fil ? (ref => GetOldestSignedSubmission(ref))

        onComplete(fOldestSigned) {
          case Success(Some(submission)) =>
            complete(ToResponseMarshallable(submission))
          case Success(None) =>
            complete(NotFound)
          case Failure(exception) =>
            log.error("Error whilst trying to check if the submission exists", exception)
            complete(InternalServerError)
        }
      }
    } ~ (get & path("institutions" / Segment / "signed" / "latest"  / Segment )) { (lei, period) =>
      oAuth2Authorization.authorizeTokenWithRule(AdminOnly) { _ =>
        val fil = selectFiling(clusterSharding, lei, YearUtils.parsePeriod(period).right.get.year, YearUtils.parsePeriod(period).right.get.quarter)
        val fLatestSigned: Future[Option[Submission]] = fil ? (ref => GetLatestSignedSubmission(ref))

        onComplete(fLatestSigned) {
          case Success(Some(submission)) =>
            complete(ToResponseMarshallable(submission))
          case Success(None) =>
            complete(NotFound)
          case Failure(exception) =>
            log.error("Error whilst trying to check if the submission exists", exception)
            complete(InternalServerError)
        }
      }
    } ~ (get & path("institutions" / Segment / "hmdafile" / "latest" / Segment )) { (lei, period) =>
      oAuth2Authorization.authorizeTokenWithRule(AdminOnly) { _ =>
        val fil = selectFiling(clusterSharding, lei, YearUtils.parsePeriod(period).right.get.year, YearUtils.parsePeriod(period).right.get.quarter)
        val fLatest: Future[Option[Submission]] = fil ? (ref => GetLatestSignedSubmission(ref))

        onComplete(fLatest) {
          case Success(Some(submission)) =>
            val csvSource = pipeDelimitedFileStream(submission.id).via(csvStreamingSupport.framingRenderer)
            // specify the filename for users that want to download
            respondWithHeader(`Content-Disposition`(attachment, Map("filename" -> s"${submission.id}.txt"))) {
              complete(OK, HttpEntity(`text/csv(UTF-8)`, csvSource))
            }
          case Success(None) =>
            complete(NotFound)
          case Failure(exception) =>
            log.error("Error whilst trying to check if the submission exists", exception)
            complete(InternalServerError)
        }
      }
    } ~ (get & path("receipt" / Segment / "hmdafile")) { rawSubmissionId =>
      oAuth2Authorization.authorizeTokenWithRule(AdminOnly) { _ =>
        validateRawSubmissionId(rawSubmissionId) match {
          case Invalid(reason) =>
            val formattedReasons = reason.mkString_(", ")
            complete((StatusCodes.BadRequest,formattedReasons))

          case Valid(submissionId) =>
            val submissionRef    = SubmissionPersistence.selectSubmissionPersistence(clusterSharding, submissionId)
            val submissionExists = submissionRef ? GetSubmission
            onComplete(submissionExists) {
              case Failure(exception) =>
                log.error("Error whilst trying to check if the submission exists", exception)
                complete(InternalServerError)

              case Success(None) =>
                complete((StatusCodes.NotFound,s"Submission with $submissionId does not exist"))

              case Success(Some(_)) =>
                val csvSource = pipeDelimitedFileStream(submissionId).via(csvStreamingSupport.framingRenderer)
                // specify the filename for users that want to download
                respondWithHeader(`Content-Disposition`(attachment, Map("filename" -> s"$submissionId.txt"))) {
                  complete(OK, HttpEntity(`text/csv(UTF-8)`, csvSource))
                }
            }
        }
      }
    } ~ (get & path("receipt" / Segment / "hmdafile" / "count")) { rawSubmissionId =>
      oAuth2Authorization.authorizeTokenWithRule(AdminOnly) { _ =>
        withRequestTimeout(countTimeout) {
          validateRawSubmissionId(rawSubmissionId) match {
            case Invalid(reason) =>
              val formattedReasons = reason.mkString_(", ")
              complete((StatusCodes.BadRequest, formattedReasons))

            case Valid(submissionId) =>
              val submissionRef = SubmissionPersistence.selectSubmissionPersistence(clusterSharding, submissionId)
              val submissionExists = submissionRef ? GetSubmission

              val lineCountResult: Future[Option[Int]] = submissionExists.flatMap {
                case None => Future.successful(None)
                case Some(_) => lineCount(submissionId).map(Some(_))
              }

              onComplete(lineCountResult) {
                case Failure(exception) =>
                  log.error("Error whilst trying to check if the submission exists", exception)
                  complete(InternalServerError)

                case Success(None) =>
                  complete((StatusCodes.NotFound, s"Submission with $submissionId does not exist"))

                case Success(Some(count)) =>
                  log.info(s"Completed line count for submission $submissionId, result: $count")
                  complete(count.toString)
              }
          }
        }
      }
    } ~ (get & path("validate" / "all" /"leis" / "submissions" / "count")) {
      oAuth2Authorization.authorizeTokenWithRule(AdminOnly) { _ =>
        withRequestTimeout(countTimeout) {
          val leiSubmissionSummaries = new LeiSubmissionSummary(log, clusterSharding)
            .leiSubmissionSummaryStream
            .toMat(Sink.seq)(Keep.right)
            .run()
            .map(_.sortBy(_.lei))

          onComplete(leiSubmissionSummaries) {
            case Failure(exception) =>
              log.error("Error whilst trying to validate lei submission counts", exception)
              complete(InternalServerError)

            case Success(result) =>
              complete(result)
          }
        }
      }
    } ~ (get & path("validate" / Segment / "submissions" / "count")) { lei =>
      oAuth2Authorization.authorizeTokenWithRule(AdminOnly) { _ =>
        withRequestTimeout(countTimeout) {
          val leiSubmissionSummaries = new LeiSubmissionSummary(log, clusterSharding)
            .leiSubmissionSummaryStreamForLei(lei)
            .toMat(Sink.seq)(Keep.right)
            .run()
            .map(_.sortBy(_.lei))

          onComplete(leiSubmissionSummaries) {
            case Failure(exception) =>
              log.error("Error whilst trying to validate lei submission counts", exception)
              complete(InternalServerError)

            case Success(result) =>
              complete(result)
          }
        }
      }
    }
    // TODO: this endpoint will get a CSV list of LEI's and return data only for the lei's that are part of the CSV list.
//    ~ (get & path("validate" / "batch" / "leis" / "all" / "submissions" / "count")) { csvLeis =>
//      oAuth2Authorization.authorizeTokenWithRule(AdminOnly) { _ =>
//        withRequestTimeout(countTimeout) {
//          val leiSubmissionSummaries = new LeiSubmissionSummary(log, clusterSharding)
//            .leiSubmissionSummaryStreamForLei()
//            .toMat(Sink.seq)(Keep.right)
//            .run()
//            .map(_.sortBy(_.lei))
//
//          onComplete(leiSubmissionSummaries) {
//            case Failure(exception) =>
//              log.error("Error whilst trying to validate lei submission counts", exception)
//              complete(InternalServerError)
//
//            case Success(result) =>
//              complete(result)
//          }
//        }
//      }
//    }

  }
}
// $COVERAGE-ON$