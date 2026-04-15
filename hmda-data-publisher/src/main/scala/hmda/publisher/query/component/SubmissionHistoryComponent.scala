package hmda.publisher.query.component

import hmda.publisher.helper.PGTableNameLoader
import hmda.publisher.query.panel.InstitutionEmailEntity
import hmda.query.DbConfiguration.dbConfig
import hmda.query.repository.TableRepository
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

trait SubmissionHistoryComponent extends PGTableNameLoader{


  import dbConfig.profile.api._

  class SubmissionHistoryTable(tag: Tag)
    extends Table[SubmissionHistoryEntity](tag, submissionHistoryTableName) with SubmissionHistoryComponent {
    def lei = column[String]("lei")
    def submissionId= column[String]("submission_id")
    def signDate = column[BigInt]("sign_date")

    def * =
      (lei, submissionId, signDate) <> (SubmissionHistoryEntity.tupled, SubmissionHistoryEntity.unapply)

  }

}
