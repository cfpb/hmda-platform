package hmda.analytics.query

import hmda.model.filing.ts.TransmittalSheet
import hmda.query.DbConfiguration.dbConfig
import slick.dbio.DBIOAction
import hmda.model.filing.ts.TsGenerators._

import scala.concurrent.duration._
import scala.concurrent.Await

trait TransmittalSheetSetup extends TransmittalSheetComponent {

  implicit val transmittalSheetRepository = new TransmittalSheetRepository(
    dbConfig)

  val db = transmittalSheetRepository.db

  val duration = 5.seconds

  val ts0 = TransmittalSheetConverter(
    tsGen.sample
      .getOrElse(TransmittalSheet())
      .copy(LEI = "B90YWS6AFX2LGWOXJ1LD")
      .copy(institutionName = "Bank 0"))

  val ts1 = TransmittalSheetConverter(
    tsGen.sample
      .getOrElse(TransmittalSheet())
      .copy(LEI = "BANK1LEIFORTEST12345")
      .copy(institutionName = "Bank 1"))

  def setup(): Unit = {
    import dbConfig.profile.api._
    val setup = db.run(
      DBIOAction.seq(
        transmittalSheetTable.schema.create,
        transmittalSheetTable ++= Seq(
          ts0,
          ts1
        )
      )
    )

    Await.result(setup, duration)
  }

  def tearDown(): Unit = {
    Await.result(transmittalSheetRepository.dropSchema(), duration)
  }

}
