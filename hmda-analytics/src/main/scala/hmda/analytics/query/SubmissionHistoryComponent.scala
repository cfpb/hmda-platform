package hmda.analytics.query

import hmda.model.filing.submission.SubmissionId
import hmda.query.DbConfiguration.dbConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait SubmissionHistoryComponent {

  import dbConfig.profile.api._

  class SubmissionHistoryRepository(config: DatabaseConfig[JdbcProfile]) {
    def fetchYearTable(year: Int): String = {
      year match {
        case 2018 => "submission_history"
        case 2019 => "submission_history2019"
        case _    => "submission_history2019"
      }
    }
    def insert(lei: String,
               submissionId: SubmissionId,
               signDate: Option[Long]): Future[Int] =
      config.db.run {
        sqlu"""INSERT INTO #${fetchYearTable(submissionId.period.toInt)}
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
