package hmda.dataBrowser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import hmda.dataBrowser.repositories._
import io.circe.Codec
import monix.eval.Task

class DataBrowserQueryService(repo2018: ModifiedLarRepository2018, repo2017: ModifiedLarRepository2017, cache: Cache) extends QueryService {
  override def fetchData(
                          queryFields: QueryFields
                        ): Source[ModifiedLarEntity, NotUsed] = {
    repo2018.find(queryFields.queryFields)
  }

  override def fetchData2017(
                          queryFields: QueryFields
                        ): Source[ModifiedLarEntity2017, NotUsed] = {
    repo2017.find(queryFields.queryFields)
  }

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

  private def cacheResult[A: Codec](cacheLookup: Task[Option[A]], onMiss: Task[A], cacheUpdate: A => Task[A]): Task[A] =
    cacheLookup.flatMap {
      case None =>
        onMiss.flatMap(cacheUpdate)

      case Some(a) =>
        Task.now(a)
    }

  override def fetchAggregate(
                               queryFields: QueryFields
                             ): Task[Seq[Aggregation]] = {
    val repo = queryFields.year match {
      case "2017" => repo2017
      case "2018" => repo2018
      case _ => repo2018
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

    val rest = fields
      .filterNot(_.name == "state")
      .filterNot(_.name == "msamd")
      .filterNot(_.name == "county")
      .filterNot(_.name == "year")
      .filterNot(_.name == "lei")

    val queryFieldCombinations = permuteQueryFields(rest)
      .map(eachList => optYear.toList ++ optState.toList ++ optMsaMd.toList ++ optCounty.toList ++ optLEI.toList ++ eachList)
      .map(eachCombination => eachCombination.sortBy(field => field.name))

    Task.gatherUnordered {
      queryFieldCombinations.map { eachCombination =>
        val fieldInfos = eachCombination.map(field => FieldInfo(field.name, field.values.mkString(",")))
        println(s"cache.find($eachCombination)")
        cacheResult(
          cacheLookup = cache.find(eachCombination),
          onMiss = repo.findAndAggregate(eachCombination),
          cacheUpdate = cache.update(eachCombination, _: Statistic)
        ).map(statistic => Aggregation(statistic.count, statistic.sum, fieldInfos))
      }
    }
  }

  override def fetchFilers(queryFields: QueryFields): Task[FilerInstitutionResponse2018] = {
    val fields = queryFields.queryFields
    val repo = queryFields.year match {
      case "2018" => repo2018
      case _ => repo2018
    }
    cacheResult(
      cacheLookup = cache.findFilers2018(fields),
      onMiss = repo.findFilers(fields).map(FilerInstitutionResponse2018(_)),
      cacheUpdate = cache.updateFilers2018(fields, _: FilerInstitutionResponse2018)
    )
  }

  override def fetchFilers2017(queryFields: QueryFields): Task[FilerInstitutionResponse2017] = {
    val fields = queryFields.queryFields
    val repo = queryFields.year match {
      case "2017" => repo2017
      case _ => repo2017
    }
    cacheResult(
      cacheLookup = cache.findFilers2017(fields),
      onMiss = repo.findFilers(fields).map(FilerInstitutionResponse2017(_)),
      cacheUpdate = cache.updateFilers2017(fields, _: FilerInstitutionResponse2017)
    )
  }

}