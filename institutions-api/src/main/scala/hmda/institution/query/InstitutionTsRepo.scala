package hmda.institution.query

import com.typesafe.config.{ Config, ConfigFactory }
import com.typesafe.scalalogging.LazyLogging
import hmda.institution.projection.InstitutionDBProjection.bankFilterList
import hmda.model.institution.Agency
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

object InstitutionTsRepo {
  private val filerCountConfig: Config = ConfigFactory.load().getConfig("counts")

  private val defaultInstitution = filerCountConfig.getString("defaultInstitution")
  private val staticTS2024 = filerCountConfig.getString("transmittalSheet.2024")
  private val staticTS2023 = filerCountConfig.getString("transmittalSheet.2023")
  private val staticTS2022 = filerCountConfig.getString("transmittalSheet.2022")
  private val staticTS2021 = filerCountConfig.getString("transmittalSheet.2021")
  private val staticTS2020 = filerCountConfig.getString("transmittalSheet.2020")
  private val staticTS2019 = filerCountConfig.getString("transmittalSheet.2019")
  private val staticTS2018 = filerCountConfig.getString("transmittalSheet.2018")
  def selectTransmittalSheet(year: Int): String = {

    year match {
      case 2018 => staticTS2018
      case 2019 => staticTS2019
      case 2020 => staticTS2020
      case 2021 => staticTS2021
      case 2022 => staticTS2022
      case 2023 => staticTS2023
      case 2024 => staticTS2024
      case _ => staticTS2024
    }
  }
}
class InstitutionTsRepo(val config: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext) extends InstitutionComponent with LazyLogging {
  import InstitutionTsRepo._

  private val instRepo = institutionRepositories.getOrElse(defaultInstitution, new InstitutionRepository(config, defaultInstitution))

  def fetchPastLarCountsForQuarterlies(year: Int, pastCount: Int): Future[Seq[QuarterlyInstitutionLarCounts]] = {
    for {
      quarterlyFilers <- instRepo.getQuarterlyFilers(bankFilterList)
      annualLarCountByLei <- getAnnualLarCountByLei(quarterlyFilers.map {
        case (lei, _, _) => lei
      }, year, pastCount)
      totalLinesPerYear <- Future(annualLarCountByLei.map {
        case (lei, year, totalLines) => lei -> AnnualLarCount(year.toString, totalLines)
      })
      linesPerYearByLei <- Future(
        totalLinesPerYear.foldLeft(Map[String, Seq[AnnualLarCount]]()) {
          case (acc, (lei, larCount)) =>
            acc.updatedWith(lei) {
              case Some(larCounts) => Some(larCounts :+ larCount)
              case None => Some(Seq(larCount))
            }
        }
      )
    } yield {
      quarterlyFilers.map {
        case (lei, name, agency) => QuarterlyInstitutionLarCounts(lei, name, Agency.valueOf(agency).fullName, linesPerYearByLei(lei))
      }
    }
  }

  private def getAnnualLarCountByLei(leis: Seq[String], year: Int, pastCount: Int): Future[Seq[(String, Int, Int)]] = {
    val leiYearTotal = (1 to pastCount).map(i => {
      val yr = s"${year - i}"
      val tr = tsRepositories(yr)
      tr.getAnnualLarCountByLei(leis)
    })

    Future.sequence(leiYearTotal).map(_.flatten)
  }

}
