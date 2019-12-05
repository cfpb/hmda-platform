package hmda.publication.lar.database

import monix.eval.Task
import slick.jdbc.{ JdbcProfile, PostgresProfile }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import cats.implicits._
import hmda.model.filing.submission.SubmissionId
import slick.basic.DatabaseConfig

object PGSlickEmailSubmissionStatusRepository {
  type Postgres = PostgresProfile.backend.Database
}

import hmda.publication.lar.database.PGSlickEmailSubmissionStatusRepository.Postgres
class PGSlickEmailSubmissionStatusRepository(databaseConfig: DatabaseConfig[JdbcProfile]) extends EmailSubmissionStatusRepository {
  val db    = databaseConfig.db
  val query = TableQuery[EmailSubmissionStatusTable]

  override def recordEmailSent(e: EmailSubmissionMetadata): Task[EmailSubmissionStatus] = {
    val status = EmailSubmissionStatus(
      lei = e.submissionId.lei,
      year = e.submissionId.period.year,
      submissionId = e.submissionId.toString,
      emailAddress = e.emailAddress,
      successful = true,
      failureReason = None
    )
    val upsert = query.insertOrUpdate(status)
    Task.deferFuture(db.run(upsert)).as(status)
  }

  override def recordEmailFailed(e: EmailSubmissionMetadata, reason: String): Task[EmailSubmissionStatus] = {
    val status = EmailSubmissionStatus(
      lei = e.submissionId.lei,
      year = e.submissionId.period.year,
      submissionId = e.submissionId.toString,
      emailAddress = e.emailAddress,
      successful = false,
      failureReason = Option(reason)
    )
    val upsert = query.insertOrUpdate(status)
    Task.deferFuture(db.run(upsert)).as(status)
  }

  override def findBySubmissionId(s: SubmissionId): Task[Option[EmailSubmissionStatus]] =
    Task.deferFuture(
      db.run(query.filter(_.submissionId === s.toString).result.headOption)
    )
}

class EmailSubmissionStatusTable(tag: Tag) extends Table[EmailSubmissionStatus](tag, "hmda_email_status") {
  val lei           = column[String]("lei")
  val year          = column[Int]("year")
  val submissionId  = column[String]("submission_id", O.PrimaryKey)
  val emailAddress  = column[String]("email_address")
  val successful    = column[Boolean]("successful")
  val failureReason = column[Option[String]]("failure_reason")

  override def * : ProvenShape[EmailSubmissionStatus] =
    (lei, year, submissionId, emailAddress, successful, failureReason) <> (EmailSubmissionStatus.tupled, EmailSubmissionStatus.unapply)
}
