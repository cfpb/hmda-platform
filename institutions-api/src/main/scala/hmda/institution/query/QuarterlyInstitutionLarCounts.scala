package hmda.institution.query

import slick.jdbc.GetResult

case class QuarterlyInstitutionLarCounts(lei: String, name: String, larCounts: Map[String, Int])
object QuarterlyInstitutionLarCounts {
  implicit val getResult: GetResult[QuarterlyInstitutionLarCounts] = GetResult(result => {
    val rs = result.rs
    val year = rs.getInt("activity_year")

    val colCount = rs.getMetaData.getColumnCount
    val larCountIdxStart = 4

    val larCounts = (for {
      col <- larCountIdxStart to colCount
    } yield (s"${year + larCountIdxStart - col - 1}", rs.getInt(col))).toMap.filter(_._2 > 0)

    QuarterlyInstitutionLarCounts(
      rs.getString("lei"),
      rs.getString("respondent_name"),
      larCounts
    )
  })
}