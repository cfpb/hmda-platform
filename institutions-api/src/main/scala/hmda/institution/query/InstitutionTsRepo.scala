package hmda.institution.query

import hmda.institution.projection.InstitutionDBProjection.bankFilterList

import scala.concurrent.Future

object InstitutionTsRepo {
  import hmda.query.DbConfiguration.dbConfig
  import dbConfig.db
  import dbConfig.profile.api._

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
        from #${s"institutions$year"} inst
        where quarterly_filer = true and not lei in ('#${bankFilterList.mkString("','")}')
         """.as[QuarterlyInstitutionLarCounts]
     db.run(query)
  }

  private def getTsSql(year: Int, pastCount: Int) =
    (1 to pastCount).map(i => {
      s"(select total_lines from ${s"transmittalsheet${year - i}"} ts where ts.lei = inst.lei order by created_at desc limit 1) as lar_prev$i"
    }).mkString(",")
}
