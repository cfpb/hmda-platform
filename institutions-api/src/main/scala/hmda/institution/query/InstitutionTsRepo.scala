package hmda.institution.query

import hmda.institution.projection.InstitutionDBProjection.bankFilterList

import scala.concurrent.Future

object InstitutionTsRepo {
  import hmda.query.DbConfiguration.dbConfig
  import dbConfig.db
  import dbConfig.profile.api._

  def fetchPastLarCountsForQuarterlies(year: Int, pastCount: Int): Future[Vector[QuarterlyInstitutionLarCounts]] = {
    val query =
      sql"""
        select lei, respondent_name, activity_year, #${getTsSql(year, pastCount)}
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
