package hmda.api.http.public

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.ApiErrorProtocol
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import hmda.api.model.public.{ HmdaFilerResponse, MsaMd, MsaMdResponse }
import hmda.api.protocol.public.HmdaFilerProtocol
import hmda.census.model.Msa
import hmda.model.fi.{ Signed, Submission, SubmissionId }
import hmda.model.institution.HmdaFiler
import hmda.persistence.HmdaSupervisor.{ FindHmdaFilerPersistence, FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.{ HmdaFilerPersistence, SubmissionPersistence }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ FindHmdaFiler, FindHmdaFilers }
import hmda.persistence.processing.SubmissionManager
import hmda.persistence.processing.SubmissionManager.GetActorRef
import hmda.validation.messages.ValidationStatsMessages.FindIrsStats
import hmda.validation.stats.SubmissionLarStats
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait HmdaFilerPaths extends HmdaCustomDirectives with HmdaFilerProtocol with ApiErrorProtocol {

  implicit val timeout: Timeout
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext

  def hmdaFilersPath(supervisor: ActorRef) = {
    path("filers") {
      encodeResponse {
        timedGet { uri =>
          val hmdaFilerPersistenceF = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]
          val filersF = for {
            a <- hmdaFilerPersistenceF
            filers <- (a ? GetState).mapTo[Set[HmdaFiler]]
          } yield filers

          sendResponse(filersF, uri)
        }
      }
    } ~
      path("filers" / Segment) { period =>
        encodeResponse {
          timedGet { uri =>
            val hmdaFilerPersistenceF = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]
            val filersF = for {
              a <- hmdaFilerPersistenceF
              filers <- (a ? FindHmdaFilers(period)).mapTo[Set[HmdaFiler]]
            } yield filers

            sendResponse(filersF, uri)
          }
        }
      }
  }

  def hmdaFilerMsasPath(supervisor: ActorRef) = {
    path("filers" / Segment / Segment / "msaMds") { (period, instId) =>
      encodeResponse {
        timedGet { uri =>
          val fSubmissions = (supervisor ? FindSubmissions(SubmissionPersistence.name, instId, period)).mapTo[ActorRef]
          val hmdaFilerPersistenceF = (supervisor ? FindHmdaFilerPersistence(HmdaFilerPersistence.name)).mapTo[ActorRef]

          val fMsas = for {
            subActor <- fSubmissions
            submissions <- (subActor ? GetState).mapTo[Seq[Submission]]

            manager <- (supervisor ? FindProcessingActor(SubmissionManager.name, getLatestSignedSub(submissions))).mapTo[ActorRef]
            larStats <- (manager ? GetActorRef(SubmissionLarStats.name)).mapTo[ActorRef]
            stats <- (larStats ? FindIrsStats(getLatestSignedSub(submissions))).mapTo[Seq[Msa]]

            a <- hmdaFilerPersistenceF
            filer <- (a ? FindHmdaFiler(instId)).mapTo[Option[HmdaFiler]]
          } yield (stats, filer)

          onComplete(fMsas) {
            case Success((msas, instOpt)) =>
              val msasJson = msas.filterNot(_.id == "NA").map(msa => MsaMd(msa.id, msa.name))
              if (instOpt.isEmpty) {
                complete(ToResponseMarshallable(StatusCodes.NotFound -> s"Unable to find institution $instId"))
              } else {
                complete(ToResponseMarshallable(MsaMdResponse(instOpt.get, msasJson)))
              }
            case Failure(e) =>
              completeWithInternalError(uri, e)
          }
        }
      }
    }
  }

  private def sendResponse(filersF: Future[Set[HmdaFiler]], uri: Uri) = {
    onComplete(filersF) {
      case Success(filers) =>
        complete(ToResponseMarshallable(HmdaFilerResponse(filers)))
      case Failure(e) =>
        completeWithInternalError(uri, e)
    }
  }

  private def getLatestSignedSub(subs: Seq[Submission]): SubmissionId = {
    subs.find(s => s.status == Signed).getOrElse(Submission()).id
  }
}
