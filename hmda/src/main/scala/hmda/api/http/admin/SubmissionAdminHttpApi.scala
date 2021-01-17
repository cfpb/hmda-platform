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
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNec
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import cats.implicits._
import com.typesafe.config.Config
import hmda.api.http.admin.SubmissionAdminHttpApi.{pipeDelimitedFileStream, validateRawSubmissionId}
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingCommands.{GetLatestSignedSubmission, GetOldestSignedSubmission}
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.persistence.filing.FilingPersistence.selectFiling
import hmda.persistence.submission.{HmdaProcessingUtils, SubmissionPersistence}
import hmda.util.http.FilingResponseUtils.failedResponse
import hmda.utils.YearUtils
import hmda.utils.YearUtils.Period
import org.slf4j.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object SubmissionAdminHttpApi {
  def create(log: Logger, config: Config, clusterSharding: ClusterSharding)(
    implicit t: Timeout,
    ec: ExecutionContext,
    system: ActorSystem[_],
    mat: Materializer
  ): OAuth2Authorization => Route =
    new SubmissionAdminHttpApi(log, config, clusterSharding)(t, ec, system, mat).routes
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
      .map(header => TsCsvParser(header, fromCassandra = true).getOrElse(TransmittalSheet()))
      .map(_.toCSV)
      .map(ByteString(_)) ++
      HmdaProcessingUtils
        .readRawData(submissionId)
        .drop(1) // drop the header which is the first line of the file
        .map(_.data)
        .map(eachLine => LarCsvParser(eachLine).getOrElse(LoanApplicationRegister()))
        .filter(lar => lar.larIdentifier.LEI != "")
        .map(_.toCSV)
        .map(ByteString(_))

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

private class SubmissionAdminHttpApi(log: Logger, config: Config, clusterSharding: ClusterSharding)(
  implicit t: Timeout,
  ec: ExecutionContext,
  system: ActorSystem[_],
  mat: Materializer
) {
  private val hmdaAdminRole: String = config.getString("keycloak.hmda.admin.role")

  private implicit val csvStreamingSupport: CsvEntityStreamingSupport =
    EntityStreamingSupport.csv()

  val routes: OAuth2Authorization => Route = { (oauth2Authorization: OAuth2Authorization) =>
    (extractUri & get & path("institutions" / Segment / "signed" / "oldest"  / Segment )) { (uri, lei, period) =>
      oauth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
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
    } ~ (extractUri & get & path("institutions" / Segment / "signed" / "latest"  / Segment )) { (uri, lei, period) =>
      oauth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
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
    } ~ (extractUri & get & path("institutions" / Segment / "hmdafile" / "latest" / Segment )) { (uri, lei, period) =>
      oauth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
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
    } ~ (extractUri & get & path("receipt" / Segment / "hmdafile")) { (uri, rawSubmissionId) =>
      oauth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
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
    }
  }
}
// $COVERAGE-ON$