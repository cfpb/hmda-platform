package hmda.validation.api.ts

import scala.concurrent.{ ExecutionContext, Future }

trait TsValidationApi {

  /*
  Gets latest timestamp from database (see S013)
   */
  //TODO: this "query" must accept some sort of unique identifier for FI as parameter
  def findTimestamp(implicit ec: ExecutionContext): Future[Long]

  /*
  Returns year to be processed (see S100)
   */
  //TODO: confirm this is queried from service. Maybe passed as parameter to validation?
  def findYearProcessed(implicit ec: ExecutionContext): Future[Int]

  /*
  Returns control number (valid respondent id / agency code combination for date processed, see S025)
   */
  //TODO: this "query" must accept some sort of unique identifier for FI as parameter
  def findControlNumber(implicit ec: ExecutionContext): Future[String]

}
