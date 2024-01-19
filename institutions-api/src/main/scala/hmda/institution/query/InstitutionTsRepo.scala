package hmda.institution.query

import com.typesafe.config.{Config, ConfigFactory}
import hmda.institution.projection.InstitutionDBProjection.bankFilterList
import hmda.model.institution.HmdaFiler

import scala.concurrent.Future

object InstitutionTsRepo {

  import hmda.query.DbConfiguration.dbConfig
  import dbConfig.db
  import dbConfig.profile.api._


  val filerCountConfig: Config = ConfigFactory.load().getConfig("counts")

  val deafultInstitution = filerCountConfig.getString("deafultInstitution")
  val staticTS2023 = filerCountConfig.getString("transmittalSheet.2023")
  val staticTS2022 = filerCountConfig.getString("transmittalSheet.2022")
  val staticTS2021 = filerCountConfig.getString("transmittalSheet.2021")
  val staticTS2020 = filerCountConfig.getString("transmittalSheet.2020")
  val staticTS2019 = filerCountConfig.getString("transmittalSheet.2019")
  val staticTS2018 = filerCountConfig.getString("transmittalSheet.2018")



  def fetchPastLarCountsForQuarterlies(year: Int, pastCount: Int): Future[Vector[QuarterlyInstitutionLarCounts]] = {
    /**
     * query generated is dynamic based on how many years back the request wants, first 3 fields don't change,
     * all subsequent fields are total_lines from prior years' transmittal sheets,
     * getTsSql generates the x number of lar counts for each prior year.
     * e.g. parameters (2022, 3) would have the lar counts for 2021, 2020, and 2019
     */


    val query =
      sql"""
        select lei, respondent_name, agency, activity_year, #${getTsSql(year, pastCount)}
        from #${deafultInstitution} inst
        where quarterly_filer = true and not lei in ('#${bankFilterList.mkString("','")}')
         """.as[QuarterlyInstitutionLarCounts]
    db.run(query)
  }

  private def getTsSql(year: Int, pastCount: Int) =
    (1 to pastCount).map(i => {

      s"(select total_lines from ${selectTransmittalSheet(year - i)} ts where ts.lei = inst.lei order by created_at desc limit 1) as lar_prev$i"
    }).mkString(",")

  def selectTransmittalSheet(year: Int): String = {

    year match {
      case 2018 => staticTS2018
      case 2019 => staticTS2019
      case 2020 => staticTS2020
      case 2021 => staticTS2021
      case 2022 => staticTS2022
      case 2023 => staticTS2023
      case _ => staticTS2023
    }
  }

}
