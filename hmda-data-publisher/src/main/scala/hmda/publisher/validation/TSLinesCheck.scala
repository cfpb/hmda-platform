package hmda.publisher.validation

import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

class TSLinesCheck(dbConfig: DatabaseConfig[JdbcProfile], tsData: TsData, larData: LarData)(implicit ec: ExecutionContext)
  extends ValidationCheck {

  case class InvalidEntry(lei: String, submisssionId: String, larCount: Option[Int], tsCount: Int)

  def check(): Future[Either[String, Unit]] = {
    import dbConfig.profile.api._
    val larByLeiCount =
      larData.query
        .groupBy(x => larData.getLei(x).toUpperCase)
        .map({ case (leiUpcase, grouped) => leiUpcase -> grouped.length })

    val checkQuery: DBIO[Seq[InvalidEntry]] = tsData.query
      .joinLeft(larByLeiCount)
      .on((ts, lar) => tsData.getLei(ts).toUpperCase === lar._1)
      .filter({
        case (tsEntry, larPerLeiCount) =>
          val expectedLeiCount = larPerLeiCount.map(_._2).getOrElse(0)
          (tsData.getTotalLines(tsEntry) =!= expectedLeiCount) && tsData.getSubmissionId(tsEntry).isDefined
      })
      .map(data =>
        (tsData.getLei(data._1), tsData.getSubmissionId(data._1).get, data._2.map(_._2), tsData.getTotalLines(data._1))
          <> (InvalidEntry.tupled, InvalidEntry.unapply _)
      )
      .result

    dbConfig.db.run(checkQuery).map(produceErrorMessage)
  }

  def produceErrorMessage(entries: Seq[InvalidEntry]): Either[String, Unit] =
    if (entries.isEmpty) {
      Right(())
    } else {
      val errs =
        entries.map(e => s"LEI: ${e.lei}, submissionId: ${e.submisssionId}, countFromTs: ${e.tsCount}, countFromLar: ${e.larCount.orNull}")
      val msg =
        s"Some of the LEIs has different counts of records in Transmittal Sheet table and in LAR table.\n" +
          errs.mkString("\n")
      Left(msg)
    }

}