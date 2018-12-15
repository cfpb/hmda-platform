package hmda.analytics.query

class TransmittalSheetComponentSpec extends TransmittalSheetAsyncSetup {

  "Transmittal Sheet Component" must {
    "find TS by LEI" in {
      val leiBank0 = "B90YWS6AFX2LGWOXJ1LD"
      transmittalSheetRepository.findByLei(leiBank0).map { xs =>
        xs.headOption.getOrElse(TransmittalSheetEntity()).lei mustBe leiBank0
      }

      transmittalSheetRepository.findByLei("").map(xs => xs.size mustBe 0)
    }
  }

}
