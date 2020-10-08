package hmda.publisher.validation

import hmda.publisher.query.component.{ PublisherComponent2018, PublisherComponent2019, PublisherComponent2020 }
import hmda.publisher.query.lar.{ LarEntityImpl2018, LarEntityImpl2019, LarEntityImpl2020, ModifiedLarEntityImpl }
import hmda.query.ts.TransmittalSheetEntity
import slick.basic.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile
import slick.lifted.{ AbstractTable, Rep, TableQuery }

import scala.concurrent.{ ExecutionContext, Future }

class TSLinesCheck(dbConfig: DatabaseConfig[JdbcProfile])(implicit ec: ExecutionContext)
  extends PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020 {

  case class InvalidEntry(lei: String, submisssionId: String, larCount: Option[Int], tsCount: Int)

  def check(year: String): Future[Either[String, Unit]] = {
    val data = year match {
      case "2018" => check2018
      case "2019" => check2019
      case "2020" => check2020
    }
    data.map(produceError(year, _))
  }

  private def check2018: Future[Seq[(InvalidEntry, String)]] = {
    val checkLar = dbConfig.db.run(
      genericCheckQuery[
        super[PublisherComponent2018].LarTable,
        super[PublisherComponent2018].TransmittalSheetTable,
        LarEntityImpl2018,
        TransmittalSheetEntity
      ](larTable2018, transmittalSheetTable2018)(_.lei, _.lei, _.totalLines, _.submissionId)
    )
    val checkMLar = dbConfig.db.run(
      genericCheckQuery[
        super[PublisherComponent2018].ModifiedLarTable,
        super[PublisherComponent2018].TransmittalSheetTable,
        ModifiedLarEntityImpl,
        TransmittalSheetEntity
      ](mlarTable2018, transmittalSheetTable2018)(_.lei, _.lei, _.totalLines, _.submissionId)
    )
    for {
      larErrs  <- checkLar
      mlarErrs <- checkMLar
    } yield larErrs ++ mlarErrs
  }

  private def check2019: Future[Seq[(InvalidEntry, String)]] = {
    val checkLar = dbConfig.db.run(
      genericCheckQuery[
        super[PublisherComponent2019].LarTable,
        super[PublisherComponent2019].TransmittalSheetTable,
        LarEntityImpl2019,
        TransmittalSheetEntity
      ](larTable2019, transmittalSheetTable2019)(_.lei, _.lei, _.totalLines, _.submissionId)
    )
    val checkMLar = dbConfig.db.run(
      genericCheckQuery[
        super[PublisherComponent2019].ModifiedLarTable,
        super[PublisherComponent2019].TransmittalSheetTable,
        ModifiedLarEntityImpl,
        TransmittalSheetEntity
      ](mlarTable2019, transmittalSheetTable2019)(_.lei, _.lei, _.totalLines, _.submissionId)
    )
    for {
      larErrs  <- checkLar
      mlarErrs <- checkMLar
    } yield larErrs ++ mlarErrs
  }

  private def check2020: Future[Seq[(InvalidEntry, String)]] =
    dbConfig.db.run(
      genericCheckQuery[
        super[PublisherComponent2020].LarTable,
        super[PublisherComponent2020].TransmittalSheetTable,
        LarEntityImpl2020,
        TransmittalSheetEntity
      ](larTable2020, transmittalSheetTable2020)(_.lei, _.lei, _.totalLines, _.submissionId)
    )

  def produceError(year: String, entries: Seq[(InvalidEntry, String)]): Either[String, Unit] =
    if (entries.isEmpty) {
      Right(())
    } else {
      val errs = entries.map({
        case (e, larTableName) =>
          s"LEI: ${e.lei}, submissionId: ${e.submisssionId}, countFromTs: ${e.tsCount}, countFromLar: ${e.larCount.orNull}, larTableName: ${larTableName}"
      })
      val msg = s"Error in data publishing for year ${year}. " +
        s"Some of the LEIs has different counts of records in Transmittal Sheet table and in LAR table.\n" +
        errs.mkString("\n")
      Left(msg)
    }

  private def genericCheckQuery[LarTable <: AbstractTable[LarEntity], TsTable <: AbstractTable[TsEntity], LarEntity, TsEntity](
                                                                                                                        ): DBIO[Seq[(InvalidEntry, String)]] = {
    import dbConfig.profile.api._
    val larByLeiCount =
      larTable
        .groupBy(x => getLeiFromLar(x).toUpperCase)
        .map({ case (leiUpcase, grouped) => leiUpcase -> grouped.length })

    tsTable
      .joinLeft(larByLeiCount)
      .on((ts, lar) => getLeiFromTs(ts).toUpperCase === lar._1)
      .filter({
        case (tsEntry, larPerLeiCount) =>
          (larPerLeiCount.isEmpty || getTotalLinesFromTs(tsEntry) =!= larPerLeiCount.map(_._2)) && getSumbissionIdFromTs(tsEntry).isDefined
      })
      .map(data =>
        (getLeiFromTs(data._1), getSumbissionIdFromTs(data._1).get, data._2.map(_._2), getTotalLinesFromTs(data._1))
          <> (InvalidEntry.tupled, InvalidEntry.unapply _)
      )
      .result
      .map(_.map(_ -> larTable.baseTableRow.tableName))
  }

}