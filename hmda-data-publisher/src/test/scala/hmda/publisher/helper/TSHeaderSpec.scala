package hmda.publisher.helper

import org.scalatest.{Matchers, WordSpec}


class TSHeaderSpec extends WordSpec with Matchers {
  val header = TsHeaderObj
  val correctTSHeader = "activity_year|calendar_quarter|lei|tax_id|agency_code|respondent_name|respondent_state|respondent_city|respondent_zip_code|lar_count" + "\n"
  val correctTSHeaderCSV = "activity_year,calendar_quarter,lei,tax_id,agency_code,respondent_name,respondent_state,respondent_city,respondent_zip_code,lar_count" + "\n"

  "TS header" should {
    "have the correct TS header string" in {
      assert(header.getTSHeader == correctTSHeader)
    }

    "have the correct TS header for csv string" in {
      assert(header.getTSHeaderCSV == correctTSHeaderCSV)
    }
  }

}