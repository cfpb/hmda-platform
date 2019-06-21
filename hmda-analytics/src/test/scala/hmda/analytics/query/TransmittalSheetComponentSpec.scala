package hmda.analytics.query

import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.ts._2018.TransmittalSheet
import hmda.query.ts._
import hmda.query.ts._2018.{TransmittalSheetConverter, TransmittalSheetEntity}

class TransmittalSheetComponentSpec extends TransmittalSheetAsyncSetup {

  var submissionId = None: Option[String]
  submissionId = Some("bank0-2018-1")

  val ts =
    TransmittalSheetConverter(tsGen.sample.getOrElse(TransmittalSheet()),
                              submissionId)

  "Transmittal Sheet Component" must {
    "find TS by LEI" in {
      val leiBank0 = "B90YWS6AFX2LGWOXJ1LD"
      transmittalSheetRepository.findByLei(leiBank0).map { xs =>
        xs.headOption.getOrElse(TransmittalSheetEntity()).lei mustBe leiBank0
      }

      transmittalSheetRepository.findByLei("").map(xs => xs.size mustBe 0)
    }
    "insert new TS Entity" in {
      transmittalSheetRepository.insert(ts).map(i => i mustBe 1)
      transmittalSheetRepository.count().map(i => i mustBe 3)
    }
    "read new TS Entity" in {
      transmittalSheetRepository.findByLei(ts.lei).map { xs =>
        xs.headOption.getOrElse(TransmittalSheetEntity()) mustBe ts
      }
      transmittalSheetRepository.count().map(i => i mustBe 3)
    }
    "delete new TS Entity" in {
      transmittalSheetRepository.deleteByLei(ts.lei).map(i => i mustBe 1)
      transmittalSheetRepository.count().map(i => i mustBe 2)
    }

  }

}
