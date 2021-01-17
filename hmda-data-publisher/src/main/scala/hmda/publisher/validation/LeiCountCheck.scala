package hmda.publisher.validation
// $COVERAGE-OFF$
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

class LeiCountCheck(dbConfig: DatabaseConfig[JdbcProfile], tsData: TsData, larData: LarData, allowedErrorMargin: Int)(
  implicit ec: ExecutionContext
) extends ValidationCheck {

  def check(): Future[Either[String, Unit]] = {

    import dbConfig.profile.api._

    val larLeiCountF: Future[Int] = dbConfig.db.run(larData.query.map(e => larData.getLei(e).toUpperCase).distinct.length.result)

    val tsLeiCountF: Future[Int] = dbConfig.db.run(tsData.query.map(e => tsData.getLei(e).toUpperCase).distinct.length.result)

    for {
      larCount <- larLeiCountF
      tsCount  <- tsLeiCountF
    } yield {
      def diffWithinMargin(count1: Int, count2: Int) = (count1 - count2).abs <= allowedErrorMargin
      val isOk                                       = diffWithinMargin(tsCount, larCount)
      Either.cond(
        isOk,
        (),
        s"Number of distinct LEIs in LAR and TS mismatch by more than allowed error margin ($allowedErrorMargin). " +
          s"LAR: $larCount, TS: $tsCount"
      )
    }

  }

}
// $COVERAGE-ON$