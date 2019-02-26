package hmda.analytics.query

import hmda.query.DbConfiguration.dbConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import scala.concurrent.Future

trait SubmissionHistoryComponent {

  import dbConfig.profile.api._

  class SubmissionHistoryRepository(tableName: String,
                                    config: DatabaseConfig[JdbcProfile]) {
    def insert(lei: String,
               submissionId: Option[String],
               signDate: Option[Long]): Future[Int] =
      config.db.run {
        sqlu"""INSERT INTO #${tableName}
           VALUES (
            ${lei.toUpperCase},
            ${submissionId},
            ${signDate}
           )
          """
      }
  }

}
