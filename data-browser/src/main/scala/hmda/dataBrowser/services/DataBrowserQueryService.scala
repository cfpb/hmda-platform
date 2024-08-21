package hmda.dataBrowser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import hmda.dataBrowser.models.Aggregation._
import hmda.dataBrowser.repositories._
import monix.eval.Task
import org.slf4j.Logger

class DataBrowserQueryService(repoLatest: ModifiedLarRepositoryLatest, repo2017: ModifiedLarRepository2017, cache: Cache, log: Logger)
    extends QueryService {
  override def fetchData(
    queryFields: QueryFields
  ): Source[ModifiedLarEntity, NotUsed] =
    repoLatest.find(queryFields.queryFields, queryFields.year.toInt)

  override def fetchData2017(
    queryFields: QueryFields
  ): Source[ModifiedLarEntity2017, NotUsed] =
    repo2017.find(queryFields.queryFields, queryFields.year.toInt)

  private def generateCombinations[T](x: List[List[T]]): List[List[T]] =
    x match {
      case Nil    => List(Nil)
      case h :: _ => h.flatMap(i => generateCombinations(x.tail).map(i :: _))
    }

  def permuteQueryFields(input: List[QueryField]): List[List[LarQueryField]] = {
    val singleElementBrowserFields: List[List[LarQueryField]] =
      input.map {
        case QueryField(name, values, dbName, isAllSelected) =>
          values
            .map(value => LarQueryField(name, value, dbName, isAllSelected))
            .toList
      }
    generateCombinations(singleElementBrowserFields)
  }

  private def cacheResult[A](cacheLookup: Task[Option[A]], onMiss: Task[A], cacheUpdate: A => Task[A]): Task[(ServedFrom, A)] =
    cacheLookup.flatMap {
      case None =>
        Task(log.debug("cache miss")) >> onMiss.flatMap(cacheUpdate).map(a => (ServedFrom.Database, a))

      case Some(a) =>
        Task(log.debug("cache hit")) >> Task.now((ServedFrom.Cache, a))
    }

  override def fetchAggregate(
    queryFields: QueryFields
  ): Task[(ServedFrom, Seq[Aggregation])] = {
    val repo = queryFields.year match {
      case "2017" => repo2017
      case _      => repoLatest
    }
    val fields = queryFields.queryFields
    val optState: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "state")
    val optMsaMd: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "msamd")
    val optCounty: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "county")
    val optLEI: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "lei")
    val optARID: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "arid")

    val geoFilter: Option[QueryField] = {
      if (optState.nonEmpty) optState
      else if (optMsaMd.nonEmpty) optMsaMd
      else optCounty
    }

    val hmdaFilters: List[QueryField] = fields
      .filterNot(_.name == "state")
      .filterNot(_.name == "msamd")
      .filterNot(_.name == "county")
      .filterNot(_.name == "year")
      .filterNot(_.name == "lei")
      .filterNot(_.name == "arid")

    val queryFieldCombinations = permuteQueryFields(hmdaFilters)

    val year = queryFields.year.toInt

    println("combinations: " + queryFieldCombinations)

    if (geoFilter.nonEmpty) {
      getMultiGeos(queryFieldCombinations, geoFilter, optLEI, year, repoLatest, cache)
     } else getNational(queryFieldCombinations, optLEI, year, repoLatest, cache)
  }

  override def fetchFilers(queryFields: QueryFields): Task[(ServedFrom, FilerInstitutionResponseLatest)] = {
    val fields = queryFields.queryFields
    cacheResult(
      cacheLookup = cache.findFilers2018(fields, queryFields.year.toInt),
      onMiss = repoLatest.findFilers(fields, queryFields.year.toInt).map(FilerInstitutionResponseLatest(_)),
      cacheUpdate = cache.updateFilers2018(fields, queryFields.year.toInt, _: FilerInstitutionResponseLatest)
    )
  }

  override def fetchFilers2017(queryFields: QueryFields): Task[(ServedFrom, FilerInstitutionResponse2017)] = {
    val fields = queryFields.queryFields
    cacheResult(
      cacheLookup = cache.findFilers2017(fields, queryFields.year.toInt),
      onMiss = repo2017.findFilers(fields, queryFields.year.toInt).map(FilerInstitutionResponse2017(_)),
      cacheUpdate = cache.updateFilers2017(fields, queryFields.year.toInt, _: FilerInstitutionResponse2017)
    )
  }

  def getNational(queryFieldCombinations: List[List[LarQueryField]], optLEI: Option[QueryField], year: Int, repo: ModifiedLarRepositoryLatest, cache: Cache): Task[(ServedFrom, Seq[Aggregation])] = {
    Task.parSequenceUnordered {
      queryFieldCombinations.map { combination =>
        val fieldInfos = combination.map(field => FieldInfo(field.name, field.value))

        println("about to get result: " + combination)
        cacheResult (
          cacheLookup = cache.find(optLEI.getOrElse(QueryField()),QueryField(), combination, year),
          onMiss = repo.findAndAggregate(optLEI.getOrElse(QueryField()), QueryField(), combination, year),
          cacheUpdate = cache.update(optLEI.getOrElse(QueryField()), QueryField(), combination, year, _: Statistic)
        ).map { case (from, statistic) => (from, Aggregation(statistic.count, statistic.sum, fieldInfos)) }
      }
    }.map(results =>
      results.foldLeft((ServedFrom.Cache: ServedFrom, List.empty[Aggregation])) {
        case ((servedAcc, aggAcc), (nextServed, nextAgg)) =>
          (servedAcc.combine(nextServed), nextAgg :: aggAcc)
      }
    )
  }

  def getMultiGeos(queryFieldCombinations: List[List[LarQueryField]], geoFilter: Option[QueryField], optLEI: Option[QueryField], year: Int, repo: ModifiedLarRepositoryLatest, cache: Cache): Task[(ServedFrom, Seq[Aggregation])] = {
    val multiGeoCombinationsSeq: Seq[Task[(ServedFrom, List[Aggregation])]] = queryFieldCombinations.map { combination =>
      val fieldInfos = combination.map(field => FieldInfo(field.name, field.value))

      val geoListTask: Seq[Task[(ServedFrom, Aggregation)]] = geoFilter.getOrElse(QueryField()).values.map { singleGeoCombination =>
        val singleGeoFilter = geoFilter.getOrElse(QueryField()).copy(values = Seq(singleGeoCombination))
        cacheResult (
          cacheLookup = cache.find(optLEI.getOrElse(QueryField()), singleGeoFilter, combination, year),
          onMiss = repo.findAndAggregate(optLEI.getOrElse(QueryField()), singleGeoFilter, combination, year),
          cacheUpdate = cache.update(optLEI.getOrElse(QueryField()), singleGeoFilter, combination, year, _: Statistic)
        ).map { case (from, statistic) => (from, Aggregation(statistic.count, statistic.sum, fieldInfos)) }
      }

      val singleGeoCombinations: Task[(ServedFrom, List[Aggregation])] = {
          val singleGeoTaskList: Task[Seq[(ServedFrom, Aggregation)]] = Task.sequence(geoListTask)
          singleGeoTaskList.map { results => results.foldLeft((ServedFrom.Cache: ServedFrom, List.empty[Aggregation])) {
            case ((servedAcc, aggAcc), (nextServed, nextAgg)) =>
            (servedAcc.combine(nextServed), nextAgg :: aggAcc)
          }
        }
      }

      singleGeoCombinations
    }

    val multiGeoCombinationsTask: Task[Seq[(ServedFrom, List[Aggregation])]] = Task.sequence(multiGeoCombinationsSeq)

    val combinedAggregation: Task[(ServedFrom, Seq[Aggregation])] = multiGeoCombinationsTask.map { results => 
      results.foldLeft((ServedFrom.Cache: ServedFrom, List.empty[Aggregation])) {
        case ((servedAcc, aggAcc), (nextServed, nextAgg)) =>
          (servedAcc.combine(nextServed), nextAgg ::: aggAcc)
      }
    }

    combinedAggregation.map(result => (result._1, sumAggregations(result._2)))
  }

  def sumAggregations(initialAgg: Seq[Aggregation]): Seq[Aggregation] = {
    var tempMap = Map[List[FieldInfo], Aggregation]()
    initialAgg.foreach{ agg =>
      val aggOption = tempMap.get(agg.fields)
      aggOption match {
        case Some(tempAgg) => {
          val newAgg = Aggregation(agg.count + tempAgg.count, agg.sum + tempAgg.sum, agg.fields)
          tempMap = tempMap + (agg.fields -> newAgg) 
        }
        case None => {
          tempMap = tempMap + (agg.fields -> agg)
        }
      }
    }
    tempMap.values.toSeq
  }
}