package hmda.data.browser.rest

import akka.NotUsed
import akka.http.scaladsl.common.{
  CsvEntityStreamingSupport,
  EntityStreamingSupport
}
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.data.browser.models.ActionTaken._
import hmda.data.browser.models.Race._
import hmda.data.browser.models.Sex._
import hmda.data.browser.models.LoanType._
import hmda.data.browser.models._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

trait DataBrowserDirectives {
  private implicit val csvStreamingSupport: CsvEntityStreamingSupport =
    EntityStreamingSupport.csv()

  def csvSource(
      s: Source[ModifiedLarEntity, NotUsed]): Source[ByteString, NotUsed] = {
    val header = Source.single(ModifiedLarEntity.header)
    val content = s.map(_.toCsv)

    (header ++ content)
      .map(ByteString(_))
      .via(csvStreamingSupport.framingRenderer)
  }

  def pipeSource(
      s: Source[ModifiedLarEntity, NotUsed]): Source[ByteString, NotUsed] = {
    val headerPipe = Source.single(ModifiedLarEntity.headerPipe)
    val contentPipe = s.map(_.toPipe)

    (headerPipe ++ contentPipe)
      .map(ByteString(_))
      .via(csvStreamingSupport.framingRenderer)
  }

  def extractActions: Directive1[BrowserField] =
    parameters("actions_taken".as(CsvSeq[String]) ? Nil)
      .flatMap { rawActionsTaken =>
        println("entered here in actions_taken: " + rawActionsTaken)
        validateActionsTaken(rawActionsTaken) match {
          case Left(invalidActions) =>
            complete((BadRequest, InvalidActions(invalidActions)))

          case Right(actionsTaken) if actionsTaken.nonEmpty =>
            provide(
              BrowserField("actions_taken",
                           actionsTaken.map(_.entryName),
                           "action_taken_type",
                           "ACTION"))

          // if the user provides no filters, it meas they want to see all actions
          case Right(_) =>
            provide(BrowserField())
        }
      }

  def extractRaces: Directive1[BrowserField] =
    parameters("races".as(CsvSeq[String]) ? Nil).flatMap { rawRaces =>
      println("entered here in races: " + rawRaces)
      validateRaces(rawRaces) match {
        case Left(invalidRaces) =>
          complete((BadRequest, InvalidRaces(invalidRaces)))

        case Right(races) if races.nonEmpty =>
          provide(
            BrowserField("races",
                         races.map(_.entryName),
                         "race_categorization",
                         "RACE"))

        // if the user provides no filters, it means they want to see all races
        case Right(_) =>
          provide(BrowserField())
      }
    }

  def extractLoanType: Directive1[BrowserField] =
    parameters("loan_types".as(CsvSeq[String]) ? Nil)
      .flatMap { rawLoanTypes =>
        validateLoanType(rawLoanTypes) match {
          case Left(invalidLoanTypes) =>
            complete((BadRequest, InvalidLoanTypes(invalidLoanTypes)))

          case Right(loanTypes) if loanTypes.nonEmpty =>
            provide(
              BrowserField("loan_types",
                           loanTypes.map(_.entryName),
                           "loan_type",
                           "LOAN_TYPES"))
          case Right(_) =>
            provide(BrowserField())
        }
      }

  def extractSexes: Directive1[BrowserField] = {
    parameters("sexes".as(CsvSeq[String]) ? Nil)
      .flatMap { rawSexes =>
        println("entered here in sexes: " + rawSexes)
        validateSexes(rawSexes) match {
          case Left(invalidSexes) =>
            complete((BadRequest, InvalidSexes(invalidSexes)))

          case Right(sexes) if sexes.nonEmpty =>
            provide(
              BrowserField("sexes",
                           sexes.map(_.entryName),
                           "sex_categorization",
                           "SEX"))

          // if the user provides no filters, it meas they want to see all actions
          case Right(_) =>
            provide(BrowserField())
        }
      }
  }

  val StateSegment: PathMatcher1[State] =
    Segment.flatMap(State.withNameInsensitiveOption)

  val MsaMdSegment: PathMatcher1[MsaMd] = IntNumber.map(MsaMd)
}

object DataBrowserDirectives extends DataBrowserDirectives
