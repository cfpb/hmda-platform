package hmda.institution.query

import hmda.model.institution.Agency
import slick.jdbc.GetResult

case class LarCountSummary(yearly: Seq[AnnualLarCount], quarterly: Seq[QuarterlyInstitutionLarCounts])
case class AnnualLarCount(year: String, count: Int)
case class QuarterlyInstitutionLarCounts(lei: String, name: String, agency: String, larCounts: Seq[AnnualLarCount])
object QuarterlyInstitutionLarCounts {
  implicit val getResult: GetResult[QuarterlyInstitutionLarCounts] = GetResult(result => {
    val rs = result.rs
    val year = rs.getInt("activity_year")

    val colCount = rs.getMetaData.getColumnCount
    val larCountIdxStart = 5

    /**
     * based on the InstitutionTsRepo.fetchPastLarCountsForQuarterlies, the first 4 fields are static (check the query to see number of static fields),
     * fields 5 and onwards contain dynamic number of lar counts based on the request.
     * */
    val larCounts = (larCountIdxStart to colCount).map(col =>
      AnnualLarCount(s"${year + larCountIdxStart - col - 1}", rs.getInt(col))
    ).filter(_.count > 0)

    QuarterlyInstitutionLarCounts(
      rs.getString("lei"),
      rs.getString("respondent_name"),
      Agency.valueOf(rs.getInt("agency")).fullName,
      larCounts
    )
  })
}