package hmda.institution.query

import slick.jdbc.GetResult

case class AnnualLarCount(year: String, count: Int)
case class QuarterlyInstitutionLarCounts(lei: String, name: String, larCounts: Seq[AnnualLarCount])
object QuarterlyInstitutionLarCounts {
  implicit val getResult: GetResult[QuarterlyInstitutionLarCounts] = GetResult(result => {
    val rs = result.rs
    val year = rs.getInt("activity_year")

    val colCount = rs.getMetaData.getColumnCount
    val larCountIdxStart = 4

    /**
     * based on the InstitutionTsRepo.fetchPastLarCountsForQuarterlies, the first 3 fields don't change,
     * fields 4 and onwards contain dynamic number of lar counts based on the request.
     * */
    val larCounts = (larCountIdxStart to colCount).map(col =>
      AnnualLarCount(s"${year + larCountIdxStart - col - 1}", rs.getInt(col))
    ).filter(_.count > 0)

    QuarterlyInstitutionLarCounts(
      rs.getString("lei"),
      rs.getString("respondent_name"),
      larCounts
    )
  })
}