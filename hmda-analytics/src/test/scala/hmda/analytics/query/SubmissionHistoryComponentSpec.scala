package hmda.analytics.query

import java.time.{ LocalDateTime, ZoneOffset }

import hmda.model.filing.submission.SubmissionId
import hmda.utils.EmbeddedPostgres
import hmda.utils.YearUtils.Period
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class SubmissionHistoryComponentSpec extends AsyncWordSpec with SubmissionHistoryComponent with EmbeddedPostgres with MustMatchers {
  "SubmissionHistoryRepository" must {
    "be able to persist data" in {
      val repo    = new SubmissionHistoryRepository(dbConfig, "submission_history") // as per hmda.sql
      val lei     = "EXAMPLE-LEI"
      val timeNow = LocalDateTime.now()

      repo
        .insert(lei, SubmissionId(lei, Period(2018, None), 1), Some(timeNow.toEpochSecond(ZoneOffset.UTC)))
        .map(res => res mustBe 1)
    }
  }

  override def bootstrapSqlFile: String = "hmda.sql"
}