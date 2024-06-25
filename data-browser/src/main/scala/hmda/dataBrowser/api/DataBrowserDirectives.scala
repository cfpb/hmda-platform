package hmda.dataBrowser.api

import akka.NotUsed
import akka.http.scaladsl.common.{ CsvEntityStreamingSupport, EntityStreamingSupport }
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.model.headers.ContentDispositionTypes.attachment
import akka.http.scaladsl.model.headers.`Content-Disposition`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.dataBrowser.models.ActionTaken._
import hmda.dataBrowser.models.ConstructionMethod._
import hmda.dataBrowser.models.DwellingCategory._
import hmda.dataBrowser.models.Ethnicity._
import hmda.dataBrowser.models.LienStatus._
import hmda.dataBrowser.models.LienStatus2017._
import hmda.dataBrowser.models.LoanProduct._
import hmda.dataBrowser.models.LoanPurpose._
import hmda.dataBrowser.models.LoanPurpose2017._
import hmda.dataBrowser.models.LoanType._
import hmda.dataBrowser.models.Race._
import hmda.dataBrowser.models.Sex._
import hmda.dataBrowser.models.State._
import hmda.dataBrowser.models.County._
import hmda.dataBrowser.models.TotalUnits._
import hmda.dataBrowser.models._
import hmda.util.LEIValidator._
import Delimiter.fileEnding
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import hmda.dataBrowser.services._
import monix.eval.Task
import hmda.dataBrowser.Settings
import enumeratum._
import org.slf4j.LoggerFactory

import scala.util.Try

trait DataBrowserDirectives extends Settings {
  val log = LoggerFactory.getLogger(getClass)
  private implicit val csvStreamingSupport: CsvEntityStreamingSupport =
    EntityStreamingSupport.csv()

  /**
    * This is an effectful function that calls out to S3 to check if data is cached or runs a query against the
    * SQL database and then persists the data in S3
    *
    * The Left part of the Either indicates that the data isn't in S3 so this is a cache miss
    * The Right part of the Either indicates a cache hit so the data is present in S3
    *
    * @param cache     the file service responsible for caching raw data
    * @param db        the query service responsible for fetching raw data from the database
    * @param queries   a list of query parameters
    * @param delimiter either commas or pipes
    * @return
    */
  def obtainDataSource(
    cache: FileService,
    db: QueryService
  )(queries: QueryFields, delimiter: Delimiter, year: String): Task[Either[Source[ByteString, NotUsed], String]] = {
    val serializedData: Source[ByteString, NotUsed] = {
      queries.year match {
        case "2017" =>
          delimiter match {
            case Commas => csvSource2017(db.fetchData2017(queries))
            case Pipes  => pipeSource2017(db.fetchData2017(queries))
          }

        case _ =>
          delimiter match {
            case Commas => csvSource(db.fetchData(queries))
            case Pipes  => pipeSource(db.fetchData(queries))
          }
      }
    }

    cache
      .retrieveDataUrl(queries.queryFields, delimiter, year)
      .flatMap {
        case Some(url) =>
          log.info("DataBrowserDirectives redirecting: {}", url)
          Task.now(Right(url))
        case None =>
          // upload the data to S3 in the background and emit the Source immediately
          log.info("DataBrowserDirectives uploading")
          cache
            .persistData(queries.queryFields, delimiter, year, serializedData)
            .startAndForget *> Task(Left(serializedData))
      }
      .onErrorHandleWith { error =>
        log.error("DataBrowserDirectives onErrorHandleWith Something failed", error)
        Task.now(Left(serializedData))
      }
  }

  def csvSource(s: Source[ModifiedLarEntity, NotUsed]): Source[ByteString, NotUsed] = {
    val header  = Source.single(ModifiedLarEntity.header)
    val content = s.map(_.toCsv)

    (header ++ content)
      .map(ByteString(_))
      .via(csvStreamingSupport.framingRenderer)
  }
  // $COVERAGE-OFF$
  def csvSource2017(s: Source[ModifiedLarEntity2017, NotUsed]): Source[ByteString, NotUsed] = {
    val header  = Source.single(ModifiedLarEntity2017.header)
    val content = s.map(_.toCsv)

    (header ++ content)
      .map(ByteString(_))
      .via(csvStreamingSupport.framingRenderer)
  }
  // $COVERAGE-ON$

  def contentDispositionHeader(queries: List[QueryField], delimiter: Delimiter)(route: Route): Route = {
    val queryName =
      queries.map(q => q.name + "_" + q.values.mkString("-")).mkString("_")
    val filename = queryName.length match {
      case x if x > 100 =>
        queryName.slice(0, 100) + md5HashString(queryName)
      case _ =>
        queryName
    }
    respondWithHeader(`Content-Disposition`(attachment, Map("filename" -> (filename + fileEnding(delimiter)))))(route)
  }

  def pipeSource(s: Source[ModifiedLarEntity, NotUsed]): Source[ByteString, NotUsed] = {
    val headerPipe  = Source.single(ModifiedLarEntity.headerPipe)
    val contentPipe = s.map(_.toPipe)

    (headerPipe ++ contentPipe)
      .map(ByteString(_))
      .via(csvStreamingSupport.framingRenderer)
  }

  // $COVERAGE-OFF$
  def pipeSource2017(s: Source[ModifiedLarEntity2017, NotUsed]): Source[ByteString, NotUsed] = {
    val headerPipe  = Source.single(ModifiedLarEntity2017.headerPipe)
    val contentPipe = s.map(_.toPipe)

    (headerPipe ++ contentPipe)
      .map(ByteString(_))
      .via(csvStreamingSupport.framingRenderer)
  }
  // $COVERAGE-ON$

  private def extractMsaMds: Directive1[Option[QueryField]] =
    parameters("msamds".as(CsvSeq[Int]) ? Nil).flatMap {
      case Nil => provide(None)
      case xs =>
        provide(Option(QueryField(name = "msamd", xs.map(_.toString), dbName = "msa_md", isAllSelected = false)))
    }

  private def extractLEIs: Directive1[Option[QueryField]] =
    parameters("leis".as(CsvSeq[String]) ? Nil).flatMap {
      case Nil => provide(None)
      case xs if xs.exists(lei => !isValidLEIFormat(lei)) =>
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, s"A Valid LEI should be 20 characters and Alphanumeric (${xs.mkString(", ")})"))
      case xs =>
        provide(Option(QueryField(name = "lei", xs.map(_.toString), dbName = "lei", isAllSelected = false)))
    }
  // $COVERAGE-OFF$
  private def extractARIDs: Directive1[Option[QueryField]] =
    parameters("arids".as(CsvSeq[String]) ? Nil).flatMap {
      case Nil => provide(None)
      case xs =>
        provide(Option(QueryField(name = "arid", xs.map(_.toString), dbName = "arid", isAllSelected = false)))
    }
  // $COVERAGE-ON$

  private def extractYears: Directive1[Option[QueryField]] =
    parameters("years".as(CsvSeq[Int]) ? Nil).flatMap {
      case Nil =>
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, "must provide years value parameter"))

      case xs if xs.exists(year => (year < 2018) || (year > 2023)) => // TODO: Change this to 2017 when 2017 is released
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, s"must provide years in the range of 2018-2023, you have provided (${xs.mkString(", ")})"))

      case xs => provide(Option(QueryField(name = "year", xs.map(_.toString), dbName = "filing_year", isAllSelected = false)))
    }

  private def extractStates(year: String): Directive1[Option[QueryField]] =
    parameters("states".as(CsvSeq[String]) ? Nil).flatMap { rawStates =>
      year match {
        case "2017" =>
          rawStates match {
            case Nil => provide(None)
            case xs =>
              provide(Option(QueryField(name = "state", xs.map(_.toString), dbName = "state", isAllSelected = false)))
          }

        case _ =>
          validateStates(rawStates) match {
            case Left(invalidStates) =>
              import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
              complete((BadRequest, InvalidStates(invalidStates)))

            case Right(states) if states.nonEmpty =>
              provide(Option(QueryField(name = "state", values = states.map(_.entryName), dbName = "state", isAllSelected = false)))

            case Right(_) =>
              provide(None)
          }
      }
    }

  private def extractCounties(year: String): Directive1[Option[QueryField]] =
    parameters("counties".as(CsvSeq[String]) ? Nil)
      .map(_.toList)
      .map(validateCounties)
      .collect {
        case Right(counties) if counties.nonEmpty && year == "2017" =>
          Option(QueryField(name = "countyCombined", values = counties, dbName = "countyCombined"))

        case Right(counties) if counties.nonEmpty =>
          Option(QueryField(name = "county", values = counties, dbName = "county"))

        case Right(_) =>
          None
      }

  private def extractActions: Directive1[Option[QueryField]] = {
    val name   = "actions_taken"
    val dbName = "action_taken_type"
    parameters("actions_taken".as(CsvSeq[String]) ? Nil)
      .map(_.toList)
      .map(validateActionsTaken)
      .collect {
        case Right(actionsTaken) if actionsTaken.nonEmpty && actionsTaken.size == ActionTaken.values.size =>
          Option(QueryField(name, actionsTaken.map(_.entryName), dbName, isAllSelected = true))

        case Right(actionsTaken) if (actionsTaken.nonEmpty && actionsTaken.size != ActionTaken.values.size) =>
          Option(QueryField(name, actionsTaken.map(_.entryName), dbName, isAllSelected = false))

        case Right(_) =>
          None
      }
  }

  private def extractAgeApplicant: Directive1[Option[QueryField]] = {
    parameters("ageapplicant".as(CsvSeq[String]) ? Nil).flatMap {
      case Nil => provide(None)
      case xs =>
        provide(Option(QueryField(name = "ageapplicant", xs.map(_.toString), dbName = "age_applicant", isAllSelected = false)))
    }
  }

  private def extractEthnicities: Directive1[Option[QueryField]] = {
    val name   = "ethnicities"
    val dbName = "ethnicity_categorization"
    parameters("ethnicities".as(CsvSeq[String]) ? Nil)
      .map(validEthnicities)
      .collect {
        case Right(ethnicities) if ethnicities.nonEmpty && ethnicities.size == Ethnicity.values.size =>
          Option(QueryField(name, ethnicities.map(_.entryName), dbName, isAllSelected = true))

        case Right(ethnicities) if ethnicities.nonEmpty && ethnicities.size != Ethnicity.values.size =>
          Option(QueryField(name, ethnicities.map(_.entryName), dbName, isAllSelected = false))

        case Right(_) => None
      }
  }

  private def extractTotalUnits: Directive1[Option[QueryField]] =
    parameters("total_units".as(CsvSeq[String]) ? Nil).flatMap { rawTotalUnits =>
      val name   = "total_units"
      val dbName = name
      validateTotalUnits(rawTotalUnits) match {
        case Left(invalidTotalUnits) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidTotalUnits(invalidTotalUnits))))

        case Right(totalUnits) if totalUnits.nonEmpty && totalUnits.size == TotalUnits.values.size =>
          provide(Option(QueryField(name, totalUnits.map(_.entryName), dbName, isAllSelected = true)))

        case Right(totalUnits) if totalUnits.nonEmpty && totalUnits.size != TotalUnits.values.size =>
          provide(Option(QueryField(name, totalUnits.map(_.entryName), dbName, isAllSelected = false)))

        case Right(_) =>
          provide(None)
      }
    }

  private def extractRaces: Directive1[Option[QueryField]] =
    parameters("races".as(CsvSeq[String]) ? Nil).flatMap { rawRaces =>
      val name   = "races"
      val dbName = "race_categorization"
      validateRaces(rawRaces) match {
        case Left(invalidRaces) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidRaces(invalidRaces))))

        case Right(races) if races.nonEmpty && races.size == Race.values.size =>
          provide(Option(QueryField(name, races.map(_.entryName), dbName, isAllSelected = true)))

        case Right(races) if races.nonEmpty && races.size != Race.values.size =>
          provide(Option(QueryField(name, races.map(_.entryName), dbName, isAllSelected = false)))
        case Right(_) =>
          provide(None)
      }
    }

  private def extractConstructionMethod: Directive1[Option[QueryField]] =
    parameters("construction_methods".as(CsvSeq[String]) ? Nil).flatMap { rawConstructionMethods =>
      val name   = "construction_methods"
      val dbName = "construction_method"
      validateConstructionMethods(rawConstructionMethods) match {
        case Left(invalidConstructionMethods) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidConstructionMethods(invalidConstructionMethods))))

        case Right(constructionMethods) if constructionMethods.nonEmpty && constructionMethods.size == ConstructionMethod.values.size =>
          provide(Option(QueryField(name, constructionMethods.map(_.entryName), dbName, isAllSelected = true)))

        case Right(constructionMethods) if constructionMethods.nonEmpty && constructionMethods.size != ConstructionMethod.values.size =>
          provide(Option(QueryField(name, constructionMethods.map(_.entryName), dbName, isAllSelected = false)))
        case Right(_) =>
          provide(None)
      }
    }

  private def extractDwellingCategories: Directive1[Option[QueryField]] =
    parameters("dwelling_categories".as(CsvSeq[String]) ? Nil).flatMap { rawDwellingCategories =>
      val name   = "dwelling_categories"
      val dbName = "dwelling_category"
      validateDwellingCategories(rawDwellingCategories) match {
        case Left(invalidDwellingCategories) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidDwellingCategories(invalidDwellingCategories))))

        case Right(dwellingCategories) if dwellingCategories.nonEmpty && dwellingCategories.size == DwellingCategory.values.size =>
          provide(Option(QueryField(name, dwellingCategories.map(_.entryName), dbName, isAllSelected = true)))

        case Right(dwellingCategories) if dwellingCategories.nonEmpty && dwellingCategories.size != DwellingCategory.values.size =>
          provide(Option(QueryField(name, dwellingCategories.map(_.entryName), dbName, isAllSelected = false)))
        case Right(_) =>
          provide(None)
      }
    }

  private def extractLienStatus(year: String): Directive1[Option[QueryField]] = {
    def validateLienStatusFunction: Seq[String] => Either[Seq[String], Seq[EnumEntry]] =
      year match {
        case "2017" => validateLienStatus2017
        case _      => validateLienStatus
      }
    parameters("lien_statuses".as(CsvSeq[String]) ? Nil).flatMap { rawLienStatuses =>
      validateLienStatusFunction(rawLienStatuses) match {
        case Left(invalidLienStatuses) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidLienStatuses(invalidLienStatuses))))

        case Right(lienStatuses) if lienStatuses.nonEmpty && lienStatuses.size == LienStatus.values.size =>
          provide(Option(QueryField("lien_statuses", lienStatuses.map(_.entryName), "lien_status", isAllSelected = true)))

        case Right(lienStatuses) if lienStatuses.nonEmpty && lienStatuses.size != LienStatus.values.size =>
          provide(Option(QueryField("lien_statuses", lienStatuses.map(_.entryName), "lien_status", isAllSelected = false)))

        case Right(_) =>
          provide(None)
      }
    }
  }

  private def extractLoanProduct: Directive1[Option[QueryField]] =
    parameters("loan_products".as(CsvSeq[String]) ? Nil).flatMap { rawLoanProducts =>
      validateLoanProducts(rawLoanProducts) match {
        case Left(invalidLoanProducts) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidLoanProducts(invalidLoanProducts))))

        case Right(loanProducts) if loanProducts.nonEmpty && loanProducts.size == LoanProduct.values.size =>
          provide(Option(QueryField("loan_products", loanProducts.map(_.entryName), "loan_product_type", isAllSelected = true)))

        case Right(loanProducts) if loanProducts.nonEmpty && loanProducts.size != LoanProduct.values.size =>
          provide(Option(QueryField("loan_products", loanProducts.map(_.entryName), "loan_product_type", isAllSelected = false)))

        case Right(_) =>
          provide(None)
      }
    }

  private def extractLoanPurpose(year: String): Directive1[Option[QueryField]] = {
    def validateLoanPurposeFunction: Seq[String] => Either[Seq[String], Seq[EnumEntry]] =
      year match {
        case "2017" => validateLoanPurpose2017
        case _      => validateLoanPurpose
      }
    parameters("loan_purposes".as(CsvSeq[String]) ? Nil).flatMap { rawLoanPurposes =>
      validateLoanPurposeFunction(rawLoanPurposes) match {
        case Left(invalidLoanPurposes) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidLoanPurposes(invalidLoanPurposes))))

        case Right(loanPurposes) if loanPurposes.nonEmpty && loanPurposes.size == LoanPurpose.values.size =>
          provide(Option(QueryField("loan_purposes", loanPurposes.map(_.entryName), "loan_purpose", isAllSelected = true)))

        case Right(loanPurposes) if loanPurposes.nonEmpty && loanPurposes.size != LoanPurpose.values.size =>
          provide(Option(QueryField("loan_purposes", loanPurposes.map(_.entryName), "loan_purpose", isAllSelected = false)))

        case Right(_) =>
          provide(None)
      }
    }
  }

  private def extractLoanType: Directive1[Option[QueryField]] =
    parameters("loan_types".as(CsvSeq[String]) ? Nil).flatMap { rawLoanTypes =>
      validateLoanType(rawLoanTypes) match {
        case Left(invalidLoanTypes) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidLoanTypes(invalidLoanTypes))))

        case Right(loanTypes) if loanTypes.nonEmpty && loanTypes.size == LoanType.values.size =>
          provide(Option(QueryField("loan_types", loanTypes.map(_.entryName), "loan_type", isAllSelected = true)))

        case Right(loanTypes) if loanTypes.nonEmpty && loanTypes.size != LoanType.values.size =>
          provide(Option(QueryField("loan_types", loanTypes.map(_.entryName), "loan_type", isAllSelected = false)))

        case Right(_) =>
          provide(None)
      }
    }

  private def extractSexes: Directive1[Option[QueryField]] =
    parameters("sexes".as(CsvSeq[String]) ? Nil).flatMap { rawSexes =>
      validateSexes(rawSexes) match {
        case Left(invalidSexes) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, InvalidSexes(invalidSexes))))

        case Right(sexes) if sexes.nonEmpty && sexes.size == Sex.values.size =>
          provide(Some(QueryField("sexes", sexes.map(_.entryName), "sex_categorization", isAllSelected = true)))

        case Right(sexes) if sexes.nonEmpty && sexes.size != Sex.values.size =>
          provide(Some(QueryField("sexes", sexes.map(_.entryName), "sex_categorization", isAllSelected = false)))

        case Right(_) =>
          provide(None)
      }
    }
  // $COVERAGE-OFF$
  private def extractPropertyType: Directive1[Option[QueryField]] =
    parameters("property_types".as(CsvSeq[String]) ? Nil).flatMap { rawPropertyTypes =>
      validateLoanType(rawPropertyTypes) match {
        case Left(invalidPropertyTypes) =>
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete((BadRequest, InvalidLoanTypes(invalidPropertyTypes)))

        case Right(propertyTypes) if propertyTypes.nonEmpty && propertyTypes.size == PropertyType.values.size =>
          provide(Option(QueryField("property_types", propertyTypes.map(_.entryName), "property_type", isAllSelected = true)))

        case Right(propertyTypes) if propertyTypes.nonEmpty && propertyTypes.size != PropertyType.values.size =>
          provide(Option(QueryField("property_types", propertyTypes.map(_.entryName), "property_type", isAllSelected = false)))

        case Right(_) =>
          provide(None)
      }
    }
  // $COVERAGE-ON$

  def extractNonMandatoryQueryFields(year: String)(innerRoute: QueryFields => Route): Route =
    year match {
      case "2017"                                                    => extractNonMandatoryQueryFields2017(year)(innerRoute)
      case yearStr if Try(yearStr.toInt).filter(_ >= 2018).isSuccess => extractNonMandatoryQueryFieldsLatest(year)(innerRoute)
      case invalidYear =>
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, InvalidYear(invalidYear)))
    }

  private def extractNonMandatoryQueryFieldsLatest(year: String)(innerRoute: QueryFields => Route): Route =
    (extractActions & extractRaces & extractSexes &
      extractLoanType & extractLoanPurpose(year) & extractLienStatus(year) &
      extractConstructionMethod & extractDwellingCategories &
      extractLoanProduct & extractTotalUnits & extractEthnicities & extractAgeApplicant) {
      (
        actionsTaken,
        races,
        sexes,
        loanTypes,
        loanPurposes,
        lienStatuses,
        constructionMethods,
        dwellingCategories,
        loanProducts,
        totalUnits,
        ethnicities,
        ageApplicant
      ) =>
        val filteredfields =
          List(
            actionsTaken,
            races,
            sexes,
            loanTypes,
            loanPurposes,
            lienStatuses,
            constructionMethods,
            dwellingCategories,
            loanProducts,
            totalUnits,
            ethnicities,
            ageApplicant
          ).flatten
        if (filteredfields.size > 2) {
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete((BadRequest, TooManyFilterCriterias()))
        } else innerRoute(QueryFields(year, filteredfields))
    }

  // $COVERAGE-OFF$
  private def extractNonMandatoryQueryFields2017(year: String)(innerRoute: QueryFields => Route): Route =
    (extractActions & extractLoanPurpose(year) & extractLienStatus(year) & extractPropertyType & extractLoanType) {
      (
        actionsTaken,
        loanPurposes,
        lienStatuses,
        propertyType,
        loanType
      ) =>
        val filteredfields =
          List(
            actionsTaken,
            loanPurposes,
            lienStatuses,
            propertyType,
            loanType
          ).flatten
        if (filteredfields.size > 2) {
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete((BadRequest, TooManyFilterCriterias()))
        } else innerRoute(QueryFields(year, filteredfields))
    }
  // $COVERAGE-ON$
  def extractCountFields(innerRoute: QueryFields => Route): Route =
    (extractYears) { (years) =>
      (extractMsaMds & extractStates(years.head.values.head)) { (msaMds, states) =>
        if (years.nonEmpty && (msaMds.nonEmpty || states.nonEmpty)) {
          extractNonMandatoryQueryFields(years.head.values.head) { nonMandatoryFields =>
            if (nonMandatoryFields.queryFields.nonEmpty) {
              import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
              complete((BadRequest, NoMandatoryFieldsInCount()))
            } else {
              innerRoute((QueryFields(years.head.values.head, List(years, msaMds, states).flatten)))
            }
          }
        } else {
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete((BadRequest, ProvideYearAndStatesOrMsaMds()))
        }
      }
    }

  def extractYearsMsaMdsStatesAndCounties(innerRoute: QueryFields => Route): Route =
    (extractYears) { (years) =>
      (extractMsaMds & extractStates(years.head.values.head) & extractCounties(years.head.values.head)) { (msaMds, states, counties) =>
        if (msaMds.nonEmpty && states.nonEmpty && counties.nonEmpty) {
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, OnlyStatesOrMsaMdsOrCountiesOrLEIs())))
        } else if (years.nonEmpty)
          innerRoute(QueryFields(years.head.values.head, List(years, msaMds, states, counties).flatten))
        else {
          import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
          complete(ToResponseMarshallable((BadRequest, ProvideYearAndStatesOrMsaMdsOrCounties())))
        }
      }
    }

  def extractMsaAndStateAndCountyAndInstitutionIdentifierBrowserFields(innerRoute: QueryFields => Route): Route =
    (extractYears) { (years) =>
      years.head.values.head match {
        case "2017" => extractMsaAndStateAndCountyAndARIDBrowserFields("2017", innerRoute)
        case year   => extractMsaAndStateAndCountyAndLEIBrowserFields(year, innerRoute)
      }
    }

  // $COVERAGE-OFF$
  def extractMsaAndStateAndCountyAndLEIBrowserFields(year: String, innerRoute: QueryFields => Route): Route =
    (extractMsaMds & extractStates(year) & extractCounties(year) & extractLEIs ) { (msaMds, states, counties, leis) =>
      if ((msaMds.nonEmpty && states.nonEmpty && counties.nonEmpty && leis.nonEmpty) || (msaMds.isEmpty && states.isEmpty && counties.isEmpty && leis.isEmpty  )) {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, OnlyStatesOrMsaMdsOrCountiesOrLEIs()))
      } else
        innerRoute(QueryFields(year, List(msaMds, states, counties, leis).flatten))
    }

  def extractMsaAndStateAndCountyAndARIDBrowserFields(year: String, innerRoute: QueryFields => Route): Route =
    (extractMsaMds & extractStates(year) & extractCounties(year) & extractARIDs) { (msaMds, states, counties, arids) =>
      if ((msaMds.nonEmpty && states.nonEmpty && counties.nonEmpty && arids.nonEmpty) || (msaMds.isEmpty && states.isEmpty && counties.isEmpty && arids.isEmpty)) {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, OnlyStatesOrMsaMdsOrCountiesOrLEIs()))
      } else
        innerRoute(QueryFields(year, List(msaMds, states, counties, arids).flatten))
    }
  // $COVERAGE-ON
  def extractNationwideMandatoryYears(innerRoute: QueryFields => Route): Route =
    (extractYears) { (years) =>
      if (years.nonEmpty)
        innerRoute(QueryFields(years.head.values.head, List(years).flatten))
      else {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, ProvideYear()))
      }
    }

  def extractFieldsForAggregation(year: String)(innerRoute: QueryFields => Route): Route =
    extractNonMandatoryQueryFields(year) { browserFields =>
      if (browserFields.queryFields.nonEmpty) innerRoute(browserFields)
      else {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, NotEnoughFilterCriterias()))
      }
    }

  def extractFieldsForCount(year: String)(innerRoute: QueryFields => Route): Route =
    extractNonMandatoryQueryFields(year) { browserFields =>
      if (browserFields.queryFields.nonEmpty) innerRoute(browserFields)
      else {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        complete((BadRequest, NotEnoughFilterCriterias()))
      }
    }

  def extractFieldsForRawQueries(year: String)(innerRoute: QueryFields => Route): Route =
    extractNonMandatoryQueryFields(year)(innerRoute)

}

object DataBrowserDirectives extends DataBrowserDirectives