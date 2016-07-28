package hmda.api.http

import java.time.Instant

import akka.Done
import akka.actor.{ ActorRef, ActorSelection, ActorSystem }
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Multipart.BodyPart
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{ Framing, Sink }
import akka.util.{ ByteString, Timeout }
import hmda.api.model._
import hmda.persistence.{ FilingPersistence, HmdaFileUpload, SubmissionPersistence }
import hmda.persistence.FilingPersistence.GetFilingByPeriod
import hmda.persistence.HmdaFileUpload.{ AddLine, _ }
import hmda.persistence.InstitutionPersistence.GetInstitutionById
import hmda.persistence.SubmissionPersistence.{ CreateSubmission, GetLatestSubmission }
import hmda.api.protocol.processing.{ FilingProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Institution, Submission }
import hmda.persistence.CommonMessages._
import hmda.persistence.{ CommonMessages, FilingPersistence, SubmissionPersistence }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import spray.json._

trait InstitutionsHttpApi extends InstitutionProtocol {

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
        val requestTime = System.currentTimeMillis()
        onComplete(fInstitutions) {
          case Success(institutions) =>
            log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
            complete(ToResponseMarshallable(Institutions(institutions)))
          case Failure(error) =>
            log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
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
          val fInstitutionDetails = institutionDetails(institutionId, institutionsActor, filingsActor)
          val requestTime = System.currentTimeMillis()
          onComplete(fInstitutionDetails) {
            case Success(institutionDetails) =>
              filingsActor ! Shutdown
              log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
              if (institutionDetails.institution.id != "")
                complete(ToResponseMarshallable(institutionDetails))
              else
                complete(HttpResponse(StatusCodes.NotFound))
            case Failure(error) =>
              filingsActor ! Shutdown
              log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
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
          implicit val ec: ExecutionContext = executor
          val fDetails: Future[FilingDetail] = filingDetailsByPeriod(period, filingsActor, submissionActor)
          val requestTime = System.currentTimeMillis()
          onComplete(fDetails) {
            case Success(filingDetails) =>
              filingsActor ! Shutdown
              submissionActor ! Shutdown
              log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
              val filing = filingDetails.filing
              if (filing.institutionId == institutionId && filing.period == period)
                complete(ToResponseMarshallable(filingDetails))
              else
                complete(HttpResponse(StatusCodes.NotFound))
            case Failure(error) =>
              filingsActor ! Shutdown
              submissionActor ! Shutdown
              log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
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
        val fFiling = (filingsActor ? GetFilingByPeriod(period)).mapTo[Filing]
        val requestTime = System.currentTimeMillis()
        onComplete(fFiling) {
          case Success(filing) =>
            if (filing.period == period) {
              submissionsActor ! CreateSubmission
              val fLatest = (submissionsActor ? GetLatestSubmission).mapTo[Submission]
              onComplete(fLatest) {
                case Success(submission) =>
                  submissionsActor ! Shutdown
                  filingsActor ! Shutdown
                  log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
                  val e = HttpEntity(ContentTypes.`application/json`, submission.toJson.toString)
                  complete(HttpResponse(StatusCodes.Created, entity = e))
                case Failure(error) =>
                  submissionsActor ! Shutdown
                  log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
                  complete(HttpResponse(StatusCodes.InternalServerError))
              }
            } else {
              complete(HttpResponse(StatusCodes.NotFound))
            }
          case Failure(error) =>
            filingsActor ! Shutdown
            submissionsActor ! Shutdown
            log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
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
          val requestTime = System.currentTimeMillis()
          onComplete(uploadedF) {
            case Success(response) =>
              processingActor ! CompleteUpload
              processingActor ! Shutdown
              log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
              complete {
                "uploaded"
              }
            case Failure(error) =>
              processingActor ! Shutdown
              log.error(error.getLocalizedMessage)
              log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
              complete {
                HttpResponse(StatusCodes.BadRequest, entity = "Invalid file format")
              }
          }

        case _ =>
          complete {
            HttpResponse(StatusCodes.BadRequest, entity = "Invalid file format")
          }
      }

    }

  val institutionSummaryPath =
    path("institutions" / Segment / "summary") { institutionId =>
      extractExecutionContext { executor =>
        val institutionsActor = system.actorSelection("/user/institutions")
        val filingsActor = system.actorOf(FilingPersistence.props(institutionId))
        implicit val ec = executor
        get {
          val requestTime = System.currentTimeMillis()
          val fInstitution = (institutionsActor ? GetInstitutionById(institutionId)).mapTo[Institution]
          val fFilings = (filingsActor ? GetState).mapTo[Seq[Filing]]
          val fSummary = for {
            institution <- fInstitution
            filings <- fFilings
          } yield InstitutionSummary(institution.id, institution.name, filings)

          onComplete(fSummary) {
            case Success(summary) =>
              filingsActor ! Shutdown
              log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
              complete(ToResponseMarshallable(summary))
            case Failure(error) =>
              filingsActor ! Shutdown
              log.debug("Elapsed time: " + (System.currentTimeMillis() - requestTime) + "ms")
              complete(HttpResponse(StatusCodes.InternalServerError))
          }
        }
      }
    }

  private def institutionDetails(institutionId: String, institutionsActor: ActorSelection, filingsActor: ActorRef)(implicit ec: ExecutionContext): Future[InstitutionDetail] = {
    val fInstitution = (institutionsActor ? GetInstitutionById(institutionId)).mapTo[Institution]
    for {
      institution <- fInstitution
      filings <- (filingsActor ? GetState).mapTo[Seq[Filing]]
    } yield InstitutionDetail(institution, filings)
  }

  private def filingDetailsByPeriod(period: String, filingsActor: ActorRef, submissionActor: ActorRef)(implicit ec: ExecutionContext): Future[FilingDetail] = {
    val fFiling = (filingsActor ? GetFilingByPeriod(period)).mapTo[Filing]
    for {
      filing <- fFiling
      submissions <- (submissionActor ? GetState).mapTo[Seq[Submission]]
    } yield FilingDetail(filing, submissions)
  }

  val institutionsRoutes =
    institutionsPath ~
      institutionByIdPath ~
      institutionSummaryPath ~
      filingByPeriodPath ~
      submissionPath ~
      uploadPath
}
