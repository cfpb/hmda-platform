package hmda.api.http.admin

import java.time.LocalDate

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.admin.AdminAporRequests.{ CreateAporRequest, ModifyAporRequest }
import hmda.api.protocol.admin.AdminAporProtocol._
import hmda.api.protocol.processing.ApiErrorProtocol
import hmda.model.apor.{ APOR, FixedRate, VariableRate }
import hmda.persistence.HmdaSupervisor.FindAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.persistence.messages.commands.apor.APORCommands.{ CreateApor, FindApor, ModifyApor }
import hmda.persistence.messages.events.apor.APOREvents.{ AporCreated, AporModified }

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

trait PublicationAdminHttpApi extends HmdaCustomDirectives with ApiErrorProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  def disclosureGenerationPath(supervisor: ActorRef) =
    path("disclosure" / Segment) { (institutionId) =>
      val fAporPersistence = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]
      extractExecutionContext { executor =>
        implicit val ec = executor
        timedPost { uri =>
          entity(as[CreateAporRequest]) { createApor =>
            val newApor = createApor.newApor
            val rateType = createApor.rateType

            val fCreated: Future[AporCreated] = for {
              a <- fAporPersistence
              c <- (a ? CreateApor(newApor, rateType)).mapTo[AporCreated]
            } yield c

            onComplete(fCreated) {
              case Success(created) =>
                complete(ToResponseMarshallable(StatusCodes.Created -> created))

              case Failure(error) =>
                completeWithInternalError(uri, error)
            }
          }
        } ~
          timedPut { uri =>
            entity(as[ModifyAporRequest]) { modifyApor =>
              val newApor = modifyApor.newApor
              val rateType = modifyApor.rateType

              val fModified: Future[Option[AporModified]] = for {
                a <- fAporPersistence
                m <- (a ? ModifyApor(newApor, rateType)).mapTo[Option[AporModified]]
              } yield m

              onComplete(fModified) {
                case Success(Some(modified)) =>
                  complete(ToResponseMarshallable(StatusCodes.Accepted -> modified))
                case Success(None) =>
                  complete(ToResponseMarshallable(StatusCodes.NotFound))
                case Failure(error) =>
                  completeWithInternalError(uri, error)
              }
            }
          }
      }
    }

  def aporReadPath(supervisor: ActorRef) =
    path("apor" / Segment / IntNumber / IntNumber / IntNumber) { (rateTypeStr, year, month, day) =>
      extractExecutionContext { executor =>
        implicit val ec = executor
        timedGet { uri =>
          val tryResponse = Try {
            val findAPORPersistence = (supervisor ? FindAPORPersistence(HmdaAPORPersistence.name)).mapTo[ActorRef]
            val rateType = rateTypeStr match {
              case "fixed" => FixedRate
              case "variable" => VariableRate
              case _ => throw new Exception("Rate Type must be 'fixed' or 'variable'")
            }

            val date = LocalDate.of(year, month, day)

            val fMaybeApor: Future[Option[APOR]] = for {
              a <- findAPORPersistence
              apor <- (a ? FindApor(rateType, date)).mapTo[Option[APOR]]
            } yield apor

            onComplete(fMaybeApor) {
              case Success(maybeApor) =>
                maybeApor match {
                  case Some(apor) => complete(ToResponseMarshallable(apor))
                  case None => complete(StatusCodes.NotFound)
                }
              case Failure(error) =>
                completeWithInternalError(uri, error)
            }
          }
          tryResponse.getOrElse(complete(StatusCodes.BadRequest))
        }

      }

    }

  def aporRoutes(supervisor: ActorRef) = aporWritePath(supervisor) ~ aporReadPath(supervisor)

}
