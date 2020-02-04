package hmda.dataBrowser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import hmda.dataBrowser.repositories.{ Cache, ModifiedLarRepository }
import io.circe.Codec
import monix.eval.Task

class DataBrowserQueryService(repo: ModifiedLarRepository, cache: Cache) extends QueryService {
  override def fetchData(
                          queries: List[QueryField]
                        ): Source[ModifiedLarEntity, NotUsed] =
    repo.find(queries)

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
                               fields: List[QueryField]
                             ): Task[Seq[Aggregation]] = {
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

  override def fetchFilers(fields: List[QueryField]): Task[FilerInstitutionResponse] =
    cacheResult(
      cacheLookup = cache.findFilers(fields),
      onMiss = repo.findFilers(fields).map(FilerInstitutionResponse(_)),
      cacheUpdate = cache.updateFilers(fields, _: FilerInstitutionResponse)
    )

}