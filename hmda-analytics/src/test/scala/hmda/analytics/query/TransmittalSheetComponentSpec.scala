package hmda.analytics.query

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.query.ts._

class TransmittalSheetComponentSpec extends TransmittalSheetAsyncSetup {

  val submissionId = Some("bank0-2018-1")

  val ts =
    TransmittalSheetConverter(tsGen.sample.getOrElse(TransmittalSheet()), submissionId)

  val quarterlyTs = ts.copy(quarter = 1, isQuarterly = Some(true), lei = s"quarterly-${ts.lei}")

  "Transmittal Sheet Component" must {
    "find TS by LEI" in {
      val leiBank0 = "B90YWS6AFX2LGWOXJ1LD"
      transmittalSheetRepository.findByLei(leiBank0).map(xs => xs.headOption.getOrElse(TransmittalSheetEntity()).lei mustBe leiBank0)

      transmittalSheetRepository.findByLei("").map(xs => xs.size mustBe 0)
    }
    "insert new TS Entity" in {
      transmittalSheetRepository.insert(ts).map(i => i mustBe 1)
      transmittalSheetRepository.count().map(i => i mustBe 3)
    }
    "read new TS Entity" in {
      transmittalSheetRepository.findByLei(ts.lei).map(xs => xs.headOption.getOrElse(TransmittalSheetEntity()) mustBe ts)
      transmittalSheetRepository.count().map(i => i mustBe 3)
    }
    "delete new TS Entity" in {
      transmittalSheetRepository.deleteByLei(ts.lei).map(i => i mustBe 1).flatMap { _ =>
        transmittalSheetRepository.count().map(i => i mustBe 2)
      }
    }
    "insert, find and delete a new quarterly TS Entity" in {
      for {
        rowsInserted <- transmittalSheetRepository.insert(quarterlyTs)
        _            = rowsInserted mustBe 1
        sheets       <- transmittalSheetRepository.findByLei(quarterlyTs.lei)
        _            = sheets.size mustBe 1
        rowsUpdated  <- transmittalSheetRepository.updateByLei(quarterlyTs.copy(name = quarterlyTs.name + "-updated"))
        _            = rowsUpdated mustBe 1
        rowsDeleted  <- transmittalSheetRepository.deleteByLeiAndQuarter(quarterlyTs.lei)
      } yield rowsDeleted mustBe 1
    }
  }
}