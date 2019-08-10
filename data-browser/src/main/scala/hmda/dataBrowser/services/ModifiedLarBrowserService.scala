package hmda.dataBrowser.services
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.dataBrowser.models._
import hmda.dataBrowser.repositories.{
  ModifiedLarAggregateCache,
  ModifiedLarRepository
}
import monix.eval.Task

class ModifiedLarBrowserService(repo: ModifiedLarRepository,
                                cache: ModifiedLarAggregateCache)
  extends BrowserService {
  override def fetchData(
                          queries: List[QueryField]): Source[ModifiedLarEntity, NotUsed] =
    repo.find(queries)

  private def generateCombinations[T](x: List[List[T]]): List[List[T]] = {
    x match {
      case Nil    => List(Nil)
      case h :: _ => h.flatMap(i => generateCombinations(x.tail).map(i :: _))
    }
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

  override def fetchAggregate(
                               fields: List[QueryField]): Task[Seq[Aggregation]] = {
    val optState: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "state")
    val optMsaMd: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "msamd")
    val optYear: Option[QueryField] =
      fields.filter(_.values.nonEmpty).find(_.name == "year")
    val rest = fields
      .filterNot(_.name == "state")
      .filterNot(_.name == "msamd")
      .filterNot(_.name == "year")

    val queryFieldCombinations = permuteQueryFields(rest)
      .map(eachList =>
        optYear.toList ++ optState.toList ++ optMsaMd.toList ++ eachList)
      .map(eachCombination => eachCombination.sortBy(field => field.name))

    Task.gatherUnordered {
      queryFieldCombinations.map { eachCombination =>
        val fieldInfos = eachCombination.map(field =>
          FieldInfo(field.name, field.values.mkString(",")))
        cache
          .find(eachCombination)
          .flatMap {
            case None =>
              repo
                .findAndAggregate(eachCombination)
                .flatMap(stat => cache.update(eachCombination, stat))

            case Some(stat) =>
              Task.now(stat)
          }
          .map(statistic =>
            Aggregation(statistic.count, statistic.sum, fieldInfos))
      }
    }
  }
}