package hmda.analytics.query

import hmda.model.filing.submission.SubmissionId
import hmda.query.DbConfiguration.dbConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait SubmissionHistoryComponent {

  import dbConfig.profile.api._

  class SubmissionHistoryRepository(config: DatabaseConfig[JdbcProfile],
                                    tableName: String) {
    def insert(lei: String,
               submissionId: SubmissionId,
               signDate: Option[Long]): Future[Int] =
      config.db.run {
        sqlu"""INSERT INTO #${tableName}
           VALUES (
            ${lei.toUpperCase},
            ${submissionId.toString},
            ${signDate}
           ) ON CONFLICT (lei, submission_id) DO UPDATE SET
           lei = ${lei.toUpperCase},
           submission_id = ${submissionId.toString},
           sign_date = ${signDate}
          """
      }
  }

}
