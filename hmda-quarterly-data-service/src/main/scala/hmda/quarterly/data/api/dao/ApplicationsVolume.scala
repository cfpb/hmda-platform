package hmda.quarterly.data.api.dao
import hmda.model.filing.lar.enums.LoanTypeEnum
import slick.jdbc.GetResult

import java.sql.Timestamp

case class ApplicationsVolume(
  lastUpdated: Timestamp,
  quarter: String,
  volume: Float,
  loanType: LoanTypeEnum
) extends AggregatedVolume
object ApplicationsVolume {
  import AggregatedVolume._
  implicit val getResults: GetResult[ApplicationsVolume] = GetResult(result => {
    val (lastUpdated, quarter, volume) = getBaseData(result)
    ApplicationsVolume(
      lastUpdated, quarter, volume,
      LoanTypeEnum.valueOf(result.rs.getInt("loan_type"))
    )
  })
}