package hmda.publisher.validation

import cats.syntax.all._
import hmda.publisher.query.component.{ PublisherComponent2018, PublisherComponent2019, PublisherComponent2020 }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

class LeiCountCheck(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020 {

  val tsRepository2018 = new TransmittalSheetRepository2018(dbConfig)
  val tsRepository2019 = new TransmittalSheetRepository2019(dbConfig)
  val tsRepository2020 = new TransmittalSheetRepository2020(dbConfig)

  val larRepository2018 = new LarRepository2018(dbConfig)
  val larRepository2019 = new LarRepository2019(dbConfig)
  val larRepository2020 = new LarRepository2020(dbConfig)

  val mlarRepository2018 = new ModifiedLarRepository2018(dbConfig)
  val mlarRepository2019 = new ModifiedLarRepository2019(dbConfig)

  def check(year: String): Future[Either[String, Unit]] = {

    val allowedErrorMargin: Int = year match {
      case "2018" => 5
      case "2019" => 0
      case "2020" => 0
    }

    val larLeiCountF: Future[Int] = year match {
      case "2018" => larRepository2018.getDistinctLeiCount
      case "2019" => larRepository2019.getDistinctLeiCount
      case "2020" => larRepository2020.getDistinctLeiCount
    }

    val mlarLeiCountF: Future[Option[Int]] = year match {
      case "2018" => mlarRepository2018.getDistinctLeiCount.map(_.some)
      case "2019" => mlarRepository2019.getDistinctLeiCount.map(_.some)
      case "2020" => Future.successful(None)
    }

    val tsLeiCountF: Future[Int] = year match {
      case "2018" => tsRepository2018.getDistinctLeiCount
      case "2018" => tsRepository2019.getDistinctLeiCount
      case "2018" => tsRepository2020.getDistinctLeiCount
    }

    for {
      larCount     <- larLeiCountF
      mlarCountOpt <- mlarLeiCountF
      tsCount      <- tsLeiCountF
    } yield {
      val mlarCount                                  = mlarCountOpt.getOrElse(larCount)
      def diffWithinMargin(count1: Int, count2: Int) = (count1 - count2).abs <= allowedErrorMargin
      val isOk =
        diffWithinMargin(larCount, mlarCount) &&
          diffWithinMargin(mlarCount, tsCount) &&
          diffWithinMargin(tsCount, larCount)
      Either.cond(
        isOk,
        (),
        s"Error in data publishing for year ${year}. " +
          s"Number of distinct LEIs in LAR, MLAR, and TS mismatch more than allowed error margin ($allowedErrorMargin). " +
          s"LAR: $larCount, MLAR: ${mlarCountOpt.getOrElse("N/A")}, TS: $tsCount"
      )
    }

  }

}