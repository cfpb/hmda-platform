package hmda.api.http

import java.time.Instant

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Framing
import akka.util.{ ByteString, Timeout }
import hmda.api.model._
import hmda.persistence.FilingPersistence.GetFilingByPeriod
import hmda.persistence.HmdaFileUpload.{ AddLine, _ }
import hmda.persistence.InstitutionPersistence.GetInstitutionById
import hmda.persistence.SubmissionPersistence.{ CreateSubmission, GetLatestSubmission }
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }
import hmda.model.fi._
import hmda.persistence.CommonMessages._
import hmda.persistence.{ FilingPersistence, SubmissionPersistence }

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

trait InstitutionsHttpApi extends InstitutionProtocol with ApiErrorProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val splitLines = Framing.delimiter(ByteString("\n"), 2048, allowTruncation = true)

  val institutionsPath =
    path("institutions") {
      val institutionsActor = system.actorSelection("/user/institutions")
      get {
        val fInstitutions = (institutionsActor ? GetState).mapTo[Set[Institution]]
        onComplete(fInstitutions) {
          case Success(institutions) =>
            complete(ToResponseMarshallable(Institutions(institutions)))
          case Failure(error) =>
            log.error(error.getLocalizedMessage)
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val institutionByIdPath =
    path("institutions" / Segment) { institutionId =>
      extractExecutionContext { executor =>
        val institutionsActor = system.actorSelection("/user/institutions")
        val filingsActor = system.actorOf(FilingPersistence.props(institutionId))
        get {
          implicit val ec: ExecutionContext = executor
          val fInstitution = (institutionsActor ? GetInstitutionById(institutionId)).mapTo[PossibleInstitution]
          val filing = (filingsActor ? GetState).mapTo[Seq[Filing]]
          onComplete(fInstitution) {
            case Success(fInstitution) =>
              fInstitution match {
                case InstitutionNotFound =>
                  val error = ErrorResponse(404, s"Institution: $institutionId not found")
                  complete(ToResponseMarshallable(StatusCodes.NotFound -> error))
                case Institution(x, y, z) =>
                  onComplete(filing) {
                    case Success(filings) =>
                      filingsActor ! Shutdown
                      if (filings.isEmpty) {
                        val error = ErrorResponse(404, s"No filings for $institutionId")
                        complete(ToResponseMarshallable(StatusCodes.NotFound -> error))
                      } else {
                        complete(ToResponseMarshallable(InstitutionDetail(Institution(x, y, z), filings)))
                      }
                    case Failure(error) =>
                      filingsActor ! Shutdown
                      log.error(error.getLocalizedMessage)
                      complete(HttpResponse(StatusCodes.InternalServerError))
                  }
              }
            case Failure(error) =>
              filingsActor ! Shutdown
              log.error(error.getLocalizedMessage)
              complete(HttpResponse(StatusCodes.InternalServerError))
          }
        }
      }
    }

  val filingByPeriodPath =
    path("institutions" / Segment / "filings" / Segment) { (institutionId, period) =>
      extractExecutionContext { executor =>
        val filingsActor = system.actorOf(FilingPersistence.props(institutionId))
        val submissionActor = system.actorOf(SubmissionPersistence.props(institutionId, period))
        get {
          val fFiling = (filingsActor ? GetFilingByPeriod(period)).mapTo[PossibleFiling]
          val fSubmission = (submissionActor ? GetState).mapTo[Seq[Submission]]
          implicit val ec: ExecutionContext = executor
          onComplete(fFiling) {
            case Success(fFiling) =>
              filingsActor ! Shutdown
              fFiling match {
                case Filing(x, y, z) =>
                  onComplete(fSubmission) {
                    case Success(fSubmission) =>
                      submissionActor ! Shutdown
                      if (fSubmission.isEmpty) {
                        val error = ErrorResponse(404, s"No submissions for the $period filing for $institutionId")
                        complete(ToResponseMarshallable(StatusCodes.NotFound -> error))
                      } else {
                        complete(ToResponseMarshallable(FilingDetail(Filing(x, y, z), fSubmission)))
                      }
                    case Failure(error) =>
                      submissionActor ! Shutdown
                      complete(HttpResponse(StatusCodes.InternalServerError))
                  }
                case FilingNotFound =>
                  val error = ErrorResponse(404, s"No $period filing for $institutionId")
                  complete(ToResponseMarshallable(StatusCodes.NotFound -> error))
              }
            case Failure(error) =>
              filingsActor ! Shutdown
              submissionActor ! Shutdown
              complete(HttpResponse(StatusCodes.InternalServerError))
          }
        }
      }
    }

  val submissionPath =
    path("institutions" / Segment / "filings" / Segment / "submissions") { (institutionId, period) =>
      post {
        implicit val ec = system.dispatcher
        val filingsActor = system.actorOf(FilingPersistence.props(institutionId))
        val submissionsActor = system.actorOf(SubmissionPersistence.props(institutionId, period))
        val fFiling = (filingsActor ? GetFilingByPeriod(period)).mapTo[PossibleFiling]
        onComplete(fFiling) {
          case Success(filing) => filing match {
            case Filing(_, _, _) =>
              filingsActor ! Shutdown
              submissionsActor ! CreateSubmission
              val fLatest = (submissionsActor ? GetLatestSubmission).mapTo[Submission]
              onComplete(fLatest) {
                case Success(submission) =>
                  submissionsActor ! Shutdown
                  complete(ToResponseMarshallable(StatusCodes.Created -> submission))
                case Failure(error) =>
                  submissionsActor ! Shutdown
                  complete(HttpResponse(StatusCodes.InternalServerError))
              }
            case FilingNotFound =>
              filingsActor ! Shutdown
              submissionsActor ! Shutdown
              val errorResponse = ErrorResponse(404, s"$period filing not found for $institutionId")
              complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
          }
          case Failure(error) =>
            filingsActor ! Shutdown
            submissionsActor ! Shutdown
            complete(HttpResponse(StatusCodes.InternalServerError))

        }
      }
    }

  val uploadPath =
    path("institutions" / Segment / "filings" / Segment / "submissions" / Segment) { (institutionId, period, submissionId) =>
      val uploadTimestamp = Instant.now.toEpochMilli
      val processingActor = createHmdaFileUpload(system, submissionId)
      fileUpload("file") {
        case (metadata, byteSource) if (metadata.fileName.endsWith(".txt")) =>
          val uploadedF = byteSource
            .via(splitLines)
            .map(_.utf8String)
            .runForeach(line => processingActor ! AddLine(uploadTimestamp, line))

          onComplete(uploadedF) {
            case Success(response) =>
              processingActor ! CompleteUpload
              processingActor ! Shutdown
              complete {
                "uploaded"
              }
            case Failure(error) =>
              processingActor ! Shutdown
              log.error(error.getLocalizedMessage)
              val errorResponse = ErrorResponse(422, "Invalid file format")
              complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
          }

        case _ =>
          val errorResponse = ErrorResponse(422, "Invalid file format")
          complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
      }

    }

  val institutionSummaryPath =
    path("institutions" / Segment / "summary") { institutionId =>
      extractExecutionContext { executor =>
        val institutionsActor = system.actorSelection("/user/institutions")
        val filingsActor = system.actorOf(FilingPersistence.props(institutionId))
        get {
          implicit val ec: ExecutionContext = executor
          val fInstitution = (institutionsActor ? GetInstitutionById(institutionId)).mapTo[PossibleInstitution]
          val filing = (filingsActor ? GetState).mapTo[Seq[Filing]]
          onComplete(fInstitution) {
            case Success(fInstitution) =>
              fInstitution match {
                case InstitutionNotFound =>
                  val error = ErrorResponse(404, s"Institution: $institutionId not found")
                  complete(ToResponseMarshallable(StatusCodes.NotFound -> error))
                case Institution(id, name, _) =>
                  onComplete(filing) {
                    case Success(filings) =>
                      filingsActor ! Shutdown
                      if (filings.isEmpty) {
                        val error = ErrorResponse(404, s"No filings for $institutionId")
                        complete(ToResponseMarshallable(StatusCodes.NotFound -> error))
                      } else {
                        complete(ToResponseMarshallable(InstitutionSummary(id, name, filings)))
                      }
                    case Failure(error) =>
                      filingsActor ! Shutdown
                      log.error(error.getLocalizedMessage)
                      complete(HttpResponse(StatusCodes.InternalServerError))
                  }
              }
            case Failure(error) =>
              filingsActor ! Shutdown
              log.error(error.getLocalizedMessage)
              complete(HttpResponse(StatusCodes.InternalServerError))
          }
        }
      }
    }

  val institutionsRoutes =
    institutionsPath ~
      institutionByIdPath ~
      institutionSummaryPath ~
      filingByPeriodPath ~
      submissionPath ~
      uploadPath
}
