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

    val larCounts = (for {
      col <- larCountIdxStart to colCount
    } yield AnnualLarCount(s"${year + larCountIdxStart - col - 1}", rs.getInt(col))).filter(_.count > 0)

    QuarterlyInstitutionLarCounts(
      rs.getString("lei"),
      rs.getString("respondent_name"),
      larCounts
    )
  })
}