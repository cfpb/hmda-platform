package hmda.data.browser.rest

import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import hmda.data.browser.models.ActionTaken._
import hmda.data.browser.models.Race._
import hmda.data.browser.models._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

trait DataBrowserDirectives {
  def extractActions: Directive1[Seq[ActionTaken]] =
    parameters("actions_taken".as(CsvSeq[Int]) ? Nil)
      .flatMap { rawActionsTaken =>
        validateActionsTaken(rawActionsTaken) match {
          case Left(invalidActions) =>
            complete((BadRequest, InvalidActions(invalidActions)))

          case Right(actionsTaken) if actionsTaken.nonEmpty =>
            provide(actionsTaken)

          // if the user provides no filters, it meas they want to see all actions
          case Right(_) =>
            provide(ActionTaken.values)
        }
      }

  def extractRaces: Directive1[Seq[Race]] =
    parameters("races".as(CsvSeq[String]) ? Nil).flatMap { rawRaces =>
      validateRaces(rawRaces) match {
        case Left(invalidRaces) =>
          complete((BadRequest, InvalidRaces(invalidRaces)))

        case Right(races) if races.nonEmpty =>
          provide(races)

        // if the user provides no filters, it means they want to see all races
        case Right(_) =>
          provide(Race.values)
      }
    }

  val StateSegment: PathMatcher1[State] =
    Segment.flatMap(State.withNameInsensitiveOption)

  val MsaMdSegment: PathMatcher1[MsaMd] = IntNumber.map(MsaMd)
}

object DataBrowserDirectives extends DataBrowserDirectives