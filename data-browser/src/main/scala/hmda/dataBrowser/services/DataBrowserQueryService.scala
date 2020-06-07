package hmda.dataBrowser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import cats.implicits._
import hmda.dataBrowser.models._
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

  def permuteQueryFields(input: List[QueryField]): List[List[QueryField]] = {
    val singleElementBrowserFields: List[List[QueryField]] =
      input.map {
        case QueryField(name, values, dbName, isAllSelected) =>
          values
            .map(value => QueryField(name, value :: Nil, dbName, isAllSelected))
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
    val optYear: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "year")
    val optLEI: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "lei")
    val optARID: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "arid")

    val rest = fields
      .filterNot(_.name == "state")
      .filterNot(_.name == "msamd")
      .filterNot(_.name == "county")
      .filterNot(_.name == "year")
      .filterNot(_.name == "lei")
      .filterNot(_.name == "arid")

    val queryFieldCombinations = permuteQueryFields(rest)
      .map(eachList =>
        optYear.toList ++ optState.toList ++ optMsaMd.toList ++ optCounty.toList ++ optLEI.toList ++ optARID.toList ++ eachList
      )
      .map(eachCombination => eachCombination.sortBy(field => field.name))

    Task.parSequenceUnordered {
      queryFieldCombinations.map { eachCombination =>
        val fieldInfos = eachCombination.map(field => FieldInfo(field.name, field.values.mkString(",")))

        // the year is a special case as the data selected depends on the year
        val year = eachCombination.find(_.name == "year").map(_.values.head).getOrElse(queryFields.year).toInt

        cacheResult(
          cacheLookup = cache.find(eachCombination),
          onMiss = repo.findAndAggregate(eachCombination, year),
          cacheUpdate = cache.update(eachCombination, _: Statistic)
        ).map { case (from, statistic) => (from, Aggregation(statistic.count, statistic.sum, fieldInfos)) }
      }
    }.map(results =>
      results.foldLeft((ServedFrom.Cache: ServedFrom, List.empty[Aggregation])) {
        case ((servedAcc, aggAcc), (nextServed, nextAgg)) =>
          (servedAcc.combine(nextServed), nextAgg :: aggAcc)
      }
    )
  }

  override def fetchFilers(queryFields: QueryFields): Task[(ServedFrom, FilerInstitutionResponse2018)] = {
    val fields = queryFields.queryFields
    cacheResult(
      cacheLookup = cache.findFilers2018(fields),
      onMiss = repoLatest.findFilers(fields, queryFields.year.toInt).map(FilerInstitutionResponse2018(_)),
      cacheUpdate = cache.updateFilers2018(fields, _: FilerInstitutionResponse2018)
    )
  }

  override def fetchFilers2017(queryFields: QueryFields): Task[(ServedFrom, FilerInstitutionResponse2017)] = {
    val fields = queryFields.queryFields
    cacheResult(
      cacheLookup = cache.findFilers2017(fields),
      onMiss = repo2017.findFilers(fields, queryFields.year.toInt).map(FilerInstitutionResponse2017(_)),
      cacheUpdate = cache.updateFilers2017(fields, _: FilerInstitutionResponse2017)
    )
  }

}