package hmda.quarterly.data.api.route.rates

import akka.http.scaladsl.server.Directives.{ complete, path, pathPrefix }
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphRoute, GraphSeriesInfo, GraphSeriesSummary }
import hmda.quarterly.data.api.route.lib.Verbiage
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType._
import hmda.quarterly.data.api.route.lib.Verbiage.Race._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object RatesGraph extends Enumeration {
  type RatesGraph = Value
  val BY_TYPE, BY_RACE = Value
}
abstract class RatesGraph(
  protected val config: String,
  protected val endpoint: String,
) extends JsonSupport {
  import RatesGraph._

  private final val verbiageConfig = Verbiage.config.getConfig(config)

  protected def title: String
  protected def subtitle: String
  protected def summaryType: RatesGraph.Value

  protected def getSummaryByType(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] = ???
  protected def getSummaryByRace(title: String, race: String): CancelableFuture[GraphSeriesSummary] = ???

  protected final val CATEGORY = verbiageConfig.getString("category")
  protected final val LABEL = verbiageConfig.getString("label")
  protected final val BY_TYPE_TITLE = verbiageConfig.getString("by_type.title")
  protected final val BY_TYPE_SUBTITLE = verbiageConfig.getString("by_type.subtitle")
  protected final val CC_BY_RACE_TITLE = verbiageConfig.getString("cc_by_race.title")
  protected final val CC_BY_RACE_SUBTITLE = verbiageConfig.getString("cc_by_race.subtitle")
  protected final val FHA_BY_RACE_TITLE = verbiageConfig.getString("fha_by_race.title")
  protected final val FHA_BY_RACE_SUBTITLE = verbiageConfig.getString("fha_by_race.subtitle")

  def getRoute: GraphRoute = new GraphRoute(title, CATEGORY, endpoint) {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          getSummary()
        )
      }
    }
  }

  private def getSummary(): CancelableFuture[GraphSeriesInfo] = summaryType match {
    case BY_TYPE =>
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
    case BY_RACE =>
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
    GraphSeriesInfo(title, subtitle, series, yLabel = LABEL)
}
