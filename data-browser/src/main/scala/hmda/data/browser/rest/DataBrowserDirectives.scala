package hmda.data.browser.rest

import akka.NotUsed
import akka.http.scaladsl.common.{
  CsvEntityStreamingSupport,
  EntityStreamingSupport
}
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.headers.ContentDispositionTypes.attachment
import akka.http.scaladsl.model.headers.`Content-Disposition`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.models.ActionTaken._
import hmda.data.browser.models.ConstructionMethod._
import hmda.data.browser.models.DwellingCategory._
import hmda.data.browser.models.Ethnicity._
import hmda.data.browser.models.LienStatus._
import hmda.data.browser.models.LoanProduct._
import hmda.data.browser.models.LoanPurpose._
import hmda.data.browser.models.LoanType._
import hmda.data.browser.models.Race._
import hmda.data.browser.models.Sex._
import hmda.data.browser.models.State._
import hmda.data.browser.models.TotalUnits._
import hmda.data.browser.models._
import io.circe.generic.auto._

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

  def contentDisposition(queries: List[QueryField])(route: Route): Route = {
    val filename =
      queries.map(q => q.name + "_" + q.values.mkString("-")).mkString("_")
    respondWithHeader(
      `Content-Disposition`(attachment, Map("filename" -> filename)))(route)
  }

  def pipeSource(
      s: Source[ModifiedLarEntity, NotUsed]): Source[ByteString, NotUsed] = {
    val headerPipe = Source.single(ModifiedLarEntity.headerPipe)
    val contentPipe = s.map(_.toPipe)

    (headerPipe ++ contentPipe)
      .map(ByteString(_))
      .via(csvStreamingSupport.framingRenderer)
  }

  private def extractMsaMds: Directive1[Option[QueryField]] =
    parameters("msamds".as(CsvSeq[Int]) ? Nil)
      .flatMap {
        case Nil => provide(None)
        case xs =>
          provide(Option(
            QueryField(name = "msamd", xs.map(_.toString), dbName = "msa_md")))
      }

  private def extractYears: Directive1[Option[QueryField]] =
    parameters("years".as(CsvSeq[Int]) ? Nil)
      .flatMap {
        case Nil => provide(None)
        case xs =>
          provide(
            Option(
              QueryField(name = "year",
                         xs.map(_.toString),
                         dbName = "filing_year")))
      }

  private def extractStates: Directive1[Option[QueryField]] =
    parameters("states".as(CsvSeq[String]) ? Nil)
      .flatMap { rawStates =>
        validateStates(rawStates) match {
          case Left(invalidStates) =>
            complete((BadRequest, InvalidStates(invalidStates)))

          case Right(states) if states.nonEmpty =>
            provide(
              Option(
                QueryField(name = "state",
                           values = states.map(_.entryName),
                           dbName = "state")))

          case Right(_) =>
            provide(None)
        }
      }

  private def extractActions: Directive1[Option[QueryField]] =
    parameters("actions_taken".as(CsvSeq[String]) ? Nil)
      .flatMap { rawActionsTaken =>
        val name = "actions_taken"
        val dbName = "action_taken_type"
        validateActionsTaken(rawActionsTaken) match {
          case Left(invalidActions) =>
            complete((BadRequest, InvalidActions(invalidActions)))

          case Right(actionsTaken) if actionsTaken.nonEmpty =>
            provide(
              Option(QueryField(name, actionsTaken.map(_.entryName), dbName)))

          case Right(_) =>
            provide(None)
        }
      }

  private def extractEthnicities: Directive1[Option[QueryField]] =
    parameters("ethnicities".as(CsvSeq[String]) ? Nil).flatMap {
      rawEthnicities =>
        val name = "ethnicities"
        val dbName = "ethnicity_categorization"
        validEthnicities(rawEthnicities) match {
          case Left(invalidEthnicities) =>
            complete((BadRequest, InvalidEthnicities(invalidEthnicities)))

          case Right(ethnicities) if ethnicities.nonEmpty =>
            provide(
              Option(QueryField(name, ethnicities.map(_.entryName), dbName)))

          case Right(_) =>
            provide(None)
        }
    }

  private def extractTotalUnits: Directive1[Option[QueryField]] =
    parameters("total_units".as(CsvSeq[String]) ? Nil).flatMap {
      rawTotalUnits =>
        val name = "total_units"
        val dbName = name
        validateTotalUnits(rawTotalUnits) match {
          case Left(invalidTotalUnits) =>
            complete((BadRequest, InvalidTotalUnits(invalidTotalUnits)))

          case Right(totalUnits) if totalUnits.nonEmpty =>
            provide(
              Option(QueryField(name, totalUnits.map(_.entryName), dbName)))

          case Right(_) =>
            provide(None)
        }
    }

  private def extractRaces: Directive1[Option[QueryField]] =
    parameters("races".as(CsvSeq[String]) ? Nil).flatMap { rawRaces =>
      val name = "races"
      val dbName = "race_categorization"
      validateRaces(rawRaces) match {
        case Left(invalidRaces) =>
          complete((BadRequest, InvalidRaces(invalidRaces)))

        case Right(races) if races.nonEmpty =>
          provide(Option(QueryField(name, races.map(_.entryName), dbName)))

        case Right(_) =>
          provide(None)
      }
    }

  private def extractConstructionMethod: Directive1[Option[QueryField]] =
    parameters("construction_methods".as(CsvSeq[String]) ? Nil)
      .flatMap { rawConstructionMethods =>
        val name = "construction_methods"
        val dbName = "construction_method"
        validateConstructionMethods(rawConstructionMethods) match {
          case Left(invalidConstructionMethods) =>
            complete(
              (BadRequest,
               InvalidConstructionMethods(invalidConstructionMethods)))

          case Right(constructionMethods) if constructionMethods.nonEmpty =>
            provide(
              Option(
                QueryField(name, constructionMethods.map(_.entryName), dbName)))

          case Right(_) =>
            provide(None)
        }
      }

  private def extractDwellingCategories: Directive1[Option[QueryField]] =
    parameters("dwelling_categories".as(CsvSeq[String]) ? Nil)
      .flatMap { rawDwellingCategories =>
        val name = "dwelling_categories"
        val dbName = "dwelling_category"
        validateDwellingCategories(rawDwellingCategories) match {
          case Left(invalidDwellingCategories) =>
            complete(
              (BadRequest,
               InvalidDwellingCategories(invalidDwellingCategories)))

          case Right(dwellingCategories) if dwellingCategories.nonEmpty =>
            provide(
              Option(
                QueryField(name, dwellingCategories.map(_.entryName), dbName)))

          case Right(_) =>
            provide(None)
        }
      }

  private def extractLienStatus: Directive1[Option[QueryField]] =
    parameters("lien_statuses".as(CsvSeq[String]) ? Nil)
      .flatMap { rawLienStatuses =>
        validateLienStatus(rawLienStatuses) match {
          case Left(invalidLienStatuses) =>
            complete((BadRequest, InvalidLienStatuses(invalidLienStatuses)))

          case Right(lienStatuses) if lienStatuses.nonEmpty =>
            provide(
              Option(
                QueryField("lien_statuses",
                           lienStatuses.map(_.entryName),
                           "lien_status")))

          case Right(_) =>
            provide(None)
        }
      }

  private def extractLoanProduct: Directive1[Option[QueryField]] =
    parameters("loan_products".as(CsvSeq[String]) ? Nil)
      .flatMap { rawLoanProducts =>
        validateLoanProducts(rawLoanProducts) match {
          case Left(invalidLoanProducts) =>
            complete((BadRequest, InvalidLoanProducts(invalidLoanProducts)))

          case Right(loanProducts) if loanProducts.nonEmpty =>
            provide(
              Option(
                QueryField("loan_products",
                           loanProducts.map(_.entryName),
                           "loan_product_type")))

          case Right(_) =>
            provide(None)
        }
      }

  private def extractLoanPurpose: Directive1[Option[QueryField]] =
    parameters("loan_purposes".as(CsvSeq[String]) ? Nil)
      .flatMap { rawLoanPurposes =>
        validateLoanPurpose(rawLoanPurposes) match {
          case Left(invalidLoanPurposes) =>
            complete((BadRequest, InvalidLoanPurposes(invalidLoanPurposes)))

          case Right(loanPurposes) if loanPurposes.nonEmpty =>
            provide(
              Option(
                QueryField("loan_purposes",
                           loanPurposes.map(_.entryName),
                           "loan_purpose")))

          case Right(_) =>
            provide(None)
        }
      }

  private def extractLoanType: Directive1[Option[QueryField]] =
    parameters("loan_types".as(CsvSeq[String]) ? Nil)
      .flatMap { rawLoanTypes =>
        validateLoanType(rawLoanTypes) match {
          case Left(invalidLoanTypes) =>
            complete((BadRequest, InvalidLoanTypes(invalidLoanTypes)))

          case Right(loanTypes) if loanTypes.nonEmpty =>
            provide(
              Option(
                QueryField("loan_types",
                           loanTypes.map(_.entryName),
                           "loan_type"))
            )
          case Right(_) =>
            provide(None)
        }
      }

  private def extractSexes: Directive1[Option[QueryField]] = {
    parameters("sexes".as(CsvSeq[String]) ? Nil)
      .flatMap { rawSexes =>
        validateSexes(rawSexes) match {
          case Left(invalidSexes) =>
            complete((BadRequest, InvalidSexes(invalidSexes)))

          case Right(sexes) if sexes.nonEmpty =>
            provide(
              Some(
                QueryField("sexes",
                           sexes.map(_.entryName),
                           "sex_categorization")))

          case Right(_) =>
            provide(None)
        }
      }
  }

  def extractNonMandatoryQueryFields(
      innerRoute: List[QueryField] => Route): Route = {
    (extractActions & extractRaces & extractSexes &
      extractLoanType & extractLoanPurpose & extractLienStatus &
      extractConstructionMethod & extractDwellingCategories & extractLoanProduct & extractTotalUnits & extractEthnicities) {
      (actionsTaken,
       races,
       sexes,
       loanTypes,
       loanPurposes,
       lienStatuses,
       constructionMethods,
       dwellingCategories,
       loanProducts,
       totalUnits,
       ethnicities) =>
        val filteredfields =
          List(actionsTaken,
               races,
               sexes,
               loanTypes,
               loanPurposes,
               lienStatuses,
               constructionMethods,
               dwellingCategories,
               loanProducts,
               totalUnits,
               ethnicities).flatten

        innerRoute(filteredfields)
    }
  }

  def extractYearsAndMsaAndStateBrowserFields(
      innerRoute: List[QueryField] => Route): Route =
    (extractYears & extractMsaMds & extractStates) { (years, msaMds, states) =>
      if (years.nonEmpty && (msaMds.nonEmpty || states.nonEmpty))
        innerRoute(List(years, msaMds, states).flatten)
      else complete(BadRequest, ProvideYearAndStatesOrMsaMds())
    }

  def extractNationwideMandatoryYears(
      innerRoute: List[QueryField] => Route): Route =
    (extractYears) { (years) =>
      if (years.nonEmpty)
        innerRoute(List(years).flatten)
      else complete(BadRequest, ProvideYear())
    }

  def extractFieldsForAggregation(
      innerRoute: List[QueryField] => Route): Route =
    extractNonMandatoryQueryFields { browserFields =>
      if (browserFields.nonEmpty) innerRoute(browserFields)
      else complete(BadRequest, NotEnoughFilterCriterias())
    }

  def extractFieldsForRawQueries(innerRoute: List[QueryField] => Route): Route =
    extractNonMandatoryQueryFields(innerRoute)
}

object DataBrowserDirectives extends DataBrowserDirectives
