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
import hmda.data.browser.models.LoanPurpose._
import hmda.data.browser.models.LienStatus._
import hmda.data.browser.models.ConstructionMethod._
import hmda.data.browser.models.DwellingCategory._
import hmda.data.browser.models.LoanProduct._
import hmda.data.browser.models.TotalUnits._
import hmda.data.browser.models.Ethnicity._
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

  def extractEthnicities: Directive1[BrowserField] =
    parameters("ethnicities".as(CsvSeq[String]) ? Nil).flatMap {
      rawEthnicities =>
        validEthnicities(rawEthnicities) match {
          case Left(invalidEthnicities) =>
            complete((BadRequest, InvalidEthnicities(invalidEthnicities)))

          case Right(ethnicities) if ethnicities.nonEmpty =>
            provide(
              BrowserField("ethnicities",
                           ethnicities.map(_.entryName),
                           "ethnicity_categorization",
                           "ETHNICITIES"))

          case Right(_) =>
            provide(BrowserField())
        }
    }

  def extractTotalUnits: Directive1[BrowserField] =
    parameters("total_units".as(CsvSeq[String]) ? Nil).flatMap {
      rawTotalUnits =>
        validateTotalUnits(rawTotalUnits) match {
          case Left(invalidTotalUnits) =>
            complete((BadRequest, InvalidTotalUnits(invalidTotalUnits)))

          case Right(totalUnits) if totalUnits.nonEmpty =>
            provide(
              BrowserField("total_units",
                           totalUnits.map(_.entryName),
                           "total_units",
                           "TOTAL_UNITS"))

          case Right(_) =>
            provide(BrowserField())
        }
    }

  def extractRaces: Directive1[BrowserField] =
    parameters("races".as(CsvSeq[String]) ? Nil).flatMap { rawRaces =>
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

  def extractConstructionMethod: Directive1[BrowserField] =
    parameters("construction_methods".as(CsvSeq[String]) ? Nil)
      .flatMap { rawConstructionMethods =>
        validateConstructionMethods(rawConstructionMethods) match {
          case Left(invalidConstructionMethods) =>
            complete(
              (BadRequest,
               InvalidConstructionMethods(invalidConstructionMethods)))

          case Right(constructionMethods) if constructionMethods.nonEmpty =>
            provide(
              BrowserField("construction_methods",
                           constructionMethods.map(_.entryName),
                           "construction_method",
                           "CONSTRUCTION_METHODS"))
          case Right(_) =>
            provide(BrowserField())
        }
      }

  def extractDwellingCategories: Directive1[BrowserField] =
    parameters("dwelling_categories".as(CsvSeq[String]) ? Nil)
      .flatMap { rawDwellingCategories =>
        validateDwellingCategories(rawDwellingCategories) match {
          case Left(invalidDwellingCategories) =>
            complete(
              (BadRequest,
               InvalidDwellingCategories(invalidDwellingCategories)))

          case Right(dwellingCategories) if dwellingCategories.nonEmpty =>
            provide(
              BrowserField("dwelling_categories",
                           dwellingCategories.map(_.entryName),
                           "dwelling_category",
                           "DWELLING_CATEGORIES"))
          case Right(_) =>
            provide(BrowserField())
        }
      }

  def extractLienStatus: Directive1[BrowserField] =
    parameters("lien_statuses".as(CsvSeq[String]) ? Nil)
      .flatMap { rawLienStatuses =>
        validateLienStatus(rawLienStatuses) match {
          case Left(invalidLienStatuses) =>
            complete((BadRequest, InvalidLienStatuses(invalidLienStatuses)))

          case Right(lienStatuses) if lienStatuses.nonEmpty =>
            provide(
              BrowserField("lien_statuses",
                           lienStatuses.map(_.entryName),
                           "lien_status",
                           "LIEN_STATUSES"))
          case Right(_) =>
            provide(BrowserField())
        }
      }

  def extractLoanProduct: Directive1[BrowserField] =
    parameters("loan_products".as(CsvSeq[String]) ? Nil)
      .flatMap { rawLoanProducts =>
        validateLoanProducts(rawLoanProducts) match {
          case Left(invalidLoanProducts) =>
            complete((BadRequest, InvalidLoanProducts(invalidLoanProducts)))

          case Right(loanProducts) if loanProducts.nonEmpty =>
            provide(
              BrowserField("loan_products",
                           loanProducts.map(_.entryName),
                           "loan_product_type",
                           "LOAN_PRODUCTS"))
          case Right(_) =>
            provide(BrowserField())
        }
      }

  def extractLoanPurpose: Directive1[BrowserField] =
    parameters("loan_purposes".as(CsvSeq[String]) ? Nil)
      .flatMap { rawLoanPurposes =>
        validateLoanPurpose(rawLoanPurposes) match {
          case Left(invalidLoanPurposes) =>
            complete((BadRequest, InvalidLoanPurposes(invalidLoanPurposes)))

          case Right(loanPurposes) if loanPurposes.nonEmpty =>
            provide(
              BrowserField("loan_purposes",
                           loanPurposes.map(_.entryName),
                           "loan_purpose",
                           "LOAN_PURPOSES"))
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

  def extractStates: Directive1[BrowserField] =
    parameters("states".as(CsvSeq[String]) ? Nil)
      .flatMap { rawStates =>
        validateActionsTaken(rawStates) match {
          case Left(invalidStates) =>
            complete((BadRequest, InvalidStates(invalidStates)))

          case Right(states) if states.nonEmpty =>
            provide(
              BrowserField("states",
                states.map(_.entryName),
                "state",
                "ACTION"))

          // if the user provides no filters, it meas they want to see all actions
          case Right(_) =>
            provide(BrowserField())
        }
      }


  def extractMsaMd: Directive1[BrowserField] =
    parameters("msamds".as(CsvSeq[String]) ? Nil)
      .flatMap { rawMsaMds =>
        validateActionsTaken(rawMsaMds) match {
          case Left(invalidActions) =>
            complete((BadRequest, InvalidMsaMds(invalidActions)))

          case Right(msaMds) if msaMds.nonEmpty =>
            provide(
              BrowserField("msamds",
                msaMds.map(_.entryName),
                "msa_md",
                "ACTION"))

          // if the user provides no filters, it meas they want to see all actions
          case Right(_) =>
            provide(BrowserField())
        }
      }

  val StateSegment: PathMatcher1[State] =
    Segment.flatMap(State.withNameInsensitiveOption)

  val MsaMdSegment: PathMatcher1[MsaMd] = IntNumber.map(MsaMd)
}

object DataBrowserDirectives extends DataBrowserDirectives
