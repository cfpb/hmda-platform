package hmda.publisher.helper

trait TSHeader {

  val TSHeader = "activity_year|calendar_quarter|lei|tax_id|agency_code|respondent_name|respondent_state|respondent_city|respondent_zip_code|lar_count" + "\n"
  val TSHeaderCSV = "activity_year,calendar_quarter,lei,tax_id,agency_code,respondent_name,respondent_state,respondent_city,respondent_zip_code,lar_count" + "\n"

}

object TsHeaderObj extends TSHeader {
  def getTSHeader = {
    TSHeader
  }
  def getTSHeaderCSV: String = {
    TSHeaderCSV
  }
}
