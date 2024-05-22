package hmda.quarterly.data.api.route.rates

import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dto.QuarterGraphData.{GraphRoute, GraphSeriesInfo, GraphSeriesSummary}
import hmda.quarterly.data.api.route.TitleSuffixAppender.addSuffix
import hmda.quarterly.data.api.route.lib.Verbiage
import hmda.quarterly.data.api.route.lib.Verbiage.DEFAULT_DECIMAL_PRECISION
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType._
import hmda.quarterly.data.api.route.lib.Verbiage.Race._
import hmda.quarterly.data.api.route.rates.RatesGraph.Category
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object RatesGraph {

  type Category = Category.Value
  final object Category extends Enumeration {
    val BY_TYPE, BY_RACE,BY_TYPE_NO_HELOC = Value
  }

  final val CATEGORY = "category"
  final val LABEL = "label"
  final val BY_TYPE_TITLE = "by_type.title"
  final val BY_TYPE_SUBTITLE = "by_type.subtitle"
  final val CC_BY_RACE_TITLE = "cc_by_race.title"
  final val CC_BY_RACE_SUBTITLE = "cc_by_race.subtitle"
  final val FHA_BY_RACE_TITLE = "fha_by_race.title"
  final val FHA_BY_RACE_SUBTITLE = "fha_by_race.subtitle"
}
abstract class RatesGraph(
  private final val config: String,
  private final val endpoint: String,
  private final val titleKey: String,
  private final val subtitleKey: String,
  private final val category: Category
) extends JsonSupport {
  import RatesGraph._

  private final val verbiageConfig = Verbiage.config.getConfig(config)
  private final val title: String = verbiageConfig.getString(titleKey)+addSuffix(endpoint)
  private final val subtitle: String = verbiageConfig.getString(subtitleKey)
  private final val categoryVerbiage: String = verbiageConfig.getString(CATEGORY)
  private final val label: String = verbiageConfig.getString(LABEL)

  protected def getSummaryByType(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] = ???
  protected def getSummaryByRace(title: String, race: String): CancelableFuture[GraphSeriesSummary] = ???
  protected def decimalPlaces: Int = DEFAULT_DECIMAL_PRECISION
  def getRoute: GraphRoute = new GraphRoute(title, categoryVerbiage, endpoint) {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          getSummary
        )
      }
    }
  }

  def getSummary: CancelableFuture[GraphSeriesInfo] = category match {
    case Category.BY_TYPE_NO_HELOC =>
      for {
        conventionalConforming <- getSummaryByType(Conventional, CONVENTIONAL_CONFORMING, conforming = true)
        conventionalNonConforming <- getSummaryByType(Conventional, CONVENTIONAL_NON_CONFORMING)
        fha <- getSummaryByType(FHAInsured, FHA)
        rhsfsa <- getSummaryByType(RHSOrFSAGuaranteed, RHS_FSA)
        va <- getSummaryByType(VAGuaranteed, VA)
      } yield getGraphSeriesInfo(
        title,
        subtitle,
        Seq(conventionalConforming, conventionalNonConforming, fha, rhsfsa, va)
      )
    case Category.BY_TYPE =>
      for {
        conventionalConforming <- getSummaryByType(Conventional, CONVENTIONAL_CONFORMING, conforming = true)
        conventionalNonConforming <- getSummaryByType(Conventional, CONVENTIONAL_NON_CONFORMING)
        fha <- getSummaryByType(FHAInsured, FHA)
        heloc <- getSummaryByType(Conventional, HELOC, heloc = true)
        rhsfsa <- getSummaryByType(RHSOrFSAGuaranteed, RHS_FSA)
        va <- getSummaryByType(VAGuaranteed, VA)
      } yield getGraphSeriesInfo(
        title,
        subtitle,
        Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
      )
    case Category.BY_RACE =>
      for {
        asian <- getSummaryByRace(ASIAN, "a")
        black <- getSummaryByRace(BLACK, "b")
        hispanic <- getSummaryByRace(HISPANIC, "h")
        white <- getSummaryByRace(WHITE, "w")
      } yield getGraphSeriesInfo(
        title,
        subtitle,
        Seq(asian, black, hispanic, white)
      )
    case invalid => throw new IllegalArgumentException(s"Invalid type: $invalid")
  }

  private def getGraphSeriesInfo(title: String, subtitle: String, series: Seq[GraphSeriesSummary]): GraphSeriesInfo =
    GraphSeriesInfo(title, subtitle, series, yLabel = label, decimalPrecision = decimalPlaces)
}
